package us.colloquy.tolstoy.client.uplink;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import us.colloquy.tolstoy.client.Tolstoy;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.model.LetterDisplay;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;
import us.colloquy.tolstoy.client.util.CommonFormatter;

/**
 * Created by Peter Gershkovich on 2/19/17.
 */


@JsType
public class DataUplink
{
    public int x;
    public int y;

    private static Timer delayTimer = null;

    @JsMethod
    public int sum()
    {
        return x + y;
    }


    @JsMethod
    public void myData(String start, String end)
    {

        if (delayTimer != null)
        {
            delayTimer.cancel();
            delayTimer = null;
        }

        delayTimer = new Timer()
        {

            public void run()
            {

                String searchString = Tolstoy.searchElastic.getText();

                //todo collect info into search facet

                TolstoyService.App.getInstance().getLettersSubset(start + ":" + end, searchString, VisualisationPanel.searchFacets, new MyAsyncCallback());
            }
        };

        delayTimer.schedule(1000);

    }


    private static class MyAsyncCallback implements AsyncCallback<ServerResponse>
    {

        public MyAsyncCallback()
        {

        }

        public void onSuccess(ServerResponse result)
        {
            VerticalPanel lettersContainer = VisualisationPanel.lettersContainer;

            lettersContainer.clear();

            //we'll populate the entire window with letters here
            CommonFormatter.formatLetterDisplay(result, lettersContainer);

            if (DOM.getElementById("li_1") != null)
            {
                ImageElement loaderImage = (ImageElement) DOM.getElementById("li_1").cast();

                loaderImage.getStyle().setVisibility(Style.Visibility.HIDDEN);
            }
        }

        public void onFailure(Throwable throwable)
        {

        }

        native void consoleLog(String message) /*-{
            console.log("me:" + message);
        }-*/;
    }


}

