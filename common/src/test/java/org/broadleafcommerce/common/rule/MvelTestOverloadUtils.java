package org.broadleafcommerce.common.rule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.broadleafcommerce.common.value.ValueAssignable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This represents a version of SelectizeCollectionUtils where static method overloading took place. The overloading has
 * since been removed in the framework implementation, but it is represented here for replication purposes as part of testing.
 * </p>
 * This class should be left as-is. Changes to this class (e.g. adding methods, etc..) can have subtle effects on the
 * ordering outcome of calls to Class.getMethods(). Since our test is so sensitive to that outcome, this class should
 * be left alone in its current state.
 *
 * @author Jeff Fischer
 */
public class MvelTestOverloadUtils {

    public static Collection intersection(final Iterable a, final Iterable b) {
        Collection response;
        if (!IterableUtils.isEmpty(a) && (a instanceof ArrayList) && !IterableUtils.isEmpty(b) && (b instanceof ArrayList)) {
            //TODO this is a bit of a hack to allow the intersection of two collections of different types. This is primarily
            //used to facilitate some MVEL execution. We really should be fixing the MVEL to call a method that retrieves
            //a list of Strings, rather than a list of ValueAssignables.
            Object aVal = ((ArrayList) a).get(0);
            Object bVal = ((ArrayList) b).get(0);
            if (aVal instanceof ValueAssignable && bVal instanceof String) {
                response = valueAssignableIntersection(a, b);
            } else {
                response = CollectionUtils.intersection(a, b);
            }
        } else {
            if (a == null || b == null) {
                return CollectionUtils.emptyCollection();
            }
            response = CollectionUtils.intersection(a, b);
        }
        return response;
    }

    public static Collection<String> valueAssignableIntersection(final Iterable<ValueAssignable> a, final Iterable<String> b) {
        List<String> temp = new ArrayList<>();

        if (!IterableUtils.isEmpty(a)) {
            for (ValueAssignable alist : a) {
                temp.add((String) alist.getValue());
            }
        }
        return CollectionUtils.intersection(temp, b);
    }

    public static Collection<String> intersection(final String a, final Iterable<String> b) {
        List<String> temp = new ArrayList<>();
        temp.add(a);
        return CollectionUtils.intersection(temp, b);
    }

}
