package cn.sumpay.tracing.agent.core.plugin;

import cn.sumpay.tracing.agent.core.plugin.bytebuddy.AbstractJunction;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.EnhancePluginDefine;
import cn.sumpay.tracing.agent.core.plugin.match.ClassMatch;
import cn.sumpay.tracing.agent.core.plugin.match.IndirectMatch;
import cn.sumpay.tracing.agent.core.plugin.match.NameMatch;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * The <code>PluginFinder</code> represents a finder , which assist to find the one
 * from the given {@link AbstractClassEnhancePluginDefine} list.
 *
 * @author heyc
 */
public class PluginFinder {
    private final Map<String, EnhancePluginDefine> nameMatchDefine = new HashMap<String, EnhancePluginDefine>();
    private final List<EnhancePluginDefine> signatureMatchDefine = new LinkedList<EnhancePluginDefine>();

    public PluginFinder(List<EnhancePluginDefine> plugins) {
        for (EnhancePluginDefine plugin : plugins) {
            ClassMatch match = plugin.enhanceClass();
            if (match == null) {
                continue;
            }
            if (match instanceof NameMatch) {
                NameMatch nameMatch = (NameMatch)match;
                nameMatchDefine.put(nameMatch.getClassName(), plugin);
            } else {
                signatureMatchDefine.add(plugin);
            }
        }
    }

    public EnhancePluginDefine find(TypeDescription typeDescription, ClassLoader classLoader) {
        String typeName = typeDescription.getTypeName();
        if (nameMatchDefine.containsKey(typeName)) {
            return nameMatchDefine.get(typeName);
        }

        for (EnhancePluginDefine pluginDefine : signatureMatchDefine) {
            IndirectMatch match = (IndirectMatch)pluginDefine.enhanceClass();
            if (match.isMatch(typeDescription)) {
                return pluginDefine;
            }
        }

        return null;
    }

    public ElementMatcher<? super TypeDescription> buildMatch() {
        ElementMatcher.Junction judge = new AbstractJunction<NamedElement>() {
            @Override
            public boolean matches(NamedElement target) {
                return nameMatchDefine.containsKey(target.getActualName());
            }
        };
        judge = judge.and(not(isInterface()));
        for (EnhancePluginDefine define : signatureMatchDefine) {
            ClassMatch match = define.enhanceClass();
            if (match instanceof IndirectMatch) {
                judge = judge.or(((IndirectMatch)match).buildJunction());
            }
        }
        return judge;
    }
}
