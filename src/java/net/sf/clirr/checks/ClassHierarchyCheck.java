//////////////////////////////////////////////////////////////////////////////
// Clirr: compares two versions of a java library for binary compatibility
// Copyright (C) 2003 - 2004  Lars K�hne
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//////////////////////////////////////////////////////////////////////////////

package net.sf.clirr.checks;

import net.sf.clirr.event.Severity;
import net.sf.clirr.event.Message;
import net.sf.clirr.framework.AbstractDiffReporter;
import net.sf.clirr.framework.ApiDiffDispatcher;
import net.sf.clirr.framework.ClassChangeCheck;
import net.sf.clirr.framework.CoIterator;
import net.sf.clirr.framework.JavaClassNameComparator;
import org.apache.bcel.classfile.JavaClass;

/**
 * Detects changes in the set of superclasses.
 *
 * @author lkuehne
 */
public final class ClassHierarchyCheck
        extends AbstractDiffReporter
        implements ClassChangeCheck
{
    private static final Message MSG_ADDED_CLASS_TO_SUPERCLASSES = new Message(5000);
    private static final Message MSG_REMOVED_CLASS_FROM_SUPERCLASSES = new Message(5001);

    /**
     * Create a new instance of this check.
     * @param dispatcher the diff dispatcher that distributes the detected changes to the listeners.
     */
    public ClassHierarchyCheck(ApiDiffDispatcher dispatcher)
    {
        super(dispatcher);
    }

    /** {@inheritDoc} */
    public boolean check(JavaClass compatBaseline, JavaClass currentVersion)
    {
        JavaClass[] compatSupers = compatBaseline.getSuperClasses();
        JavaClass[] currentSupers = currentVersion.getSuperClasses();

        boolean isThrowable = false;
        for (int i = 0; i < compatSupers.length; i++)
        {
            JavaClass javaClass = compatSupers[i];
            if ("java.lang.Throwable".equals(javaClass.getClassName()))
            {
                isThrowable = true;
            }
        }

        final String className = compatBaseline.getClassName();

        CoIterator iter = new CoIterator(
            JavaClassNameComparator.COMPARATOR, compatSupers, currentSupers);

        while (iter.hasNext())
        {
            iter.next();
            JavaClass baselineSuper = (JavaClass) iter.getLeft();
            JavaClass currentSuper = (JavaClass) iter.getRight();

            if (baselineSuper == null)
            {
                Severity severity;
                if (isThrowable)
                {
                    severity = Severity.WARNING;
                }
                else
                {
                    severity = Severity.INFO;
                }

                log(MSG_ADDED_CLASS_TO_SUPERCLASSES,
                    severity, className, null, null,
                    new String[] {currentSuper.getClassName()});
            }
            else if (currentSuper == null)
            {
                log(MSG_REMOVED_CLASS_FROM_SUPERCLASSES,
                    Severity.ERROR, className, null, null,
                    new String[] {baselineSuper.getClassName()});
            }
        }

        return true;
    }
}
