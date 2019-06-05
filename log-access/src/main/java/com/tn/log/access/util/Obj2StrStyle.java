package com.tn.log.access.util;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author chenck
 * @date 2019/5/10 11:10
 */
public class Obj2StrStyle {

    public static final SimpleStyle SIMPLE_STYLE = new SimpleStyle();

    public static final class SimpleStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        /**
         * <p>Constructor.</p>
         *
         * <p>Use the static constant rather than instantiating.</p>
         */
        SimpleStyle() {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
            this.setUseFieldNames(true);
            this.setContentStart("");
            this.setContentEnd("");
        }

        /**
         * <p>Ensure <code>Singleton</ode> after serialization.</p>
         *
         * @return the singleton
         */
        private Object readResolve() {
            return Obj2StrStyle.SIMPLE_STYLE;
        }
    }
}
