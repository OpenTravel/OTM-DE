
package org.opentravel.schemas.views.propertyview;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

public interface PropertySetter {
    public IPropertyDescriptor getDescriptor();

    public void setValue(Object value);

    public Object getValue();

    public String getId();
}