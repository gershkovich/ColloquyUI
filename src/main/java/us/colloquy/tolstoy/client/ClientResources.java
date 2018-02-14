package us.colloquy.tolstoy.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;

/**
 * Created by petergershkovich on 12/25/15.
 */
public interface ClientResources extends ClientBundle
{
    public static final ClientResources INSTANCE =  GWT.create(ClientResources.class);

    @Source("main.css")
    public CssResource mainCss();


    @Source("manual.pdf")
    public DataResource ownersManual();
}