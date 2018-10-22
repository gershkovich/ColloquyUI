package us.colloquy.tolstoy.client.uplink;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import us.colloquy.tolstoy.client.Tolstoy;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;
import us.colloquy.tolstoy.client.util.CommonFormatter;

/**
 * Created by Peter Gershkovich on 2/19/17.
 */


@JsType
@SuppressWarnings("unusable-by-js")
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
    public static void getDocumentsByRange(String start, String end)
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

                String searchString = Tolstoy.searchTextBox.getText();

                TolstoyService.App.getInstance().getLettersSubset(start + ":" + end, searchString, VisualisationPanel.searchFacets, new MyAsyncCallback());
            }
        };

        delayTimer.schedule(1000);
    }

    @JsMethod
    public static void lookupDocument(String documentId)
    {
        if ( delayTimer != null )
        {
            delayTimer.cancel();
            delayTimer = null;
        }

        delayTimer = new Timer()
        {
            public void run()
            {
                performDocumentLookup( documentId );
            }
        };

        delayTimer.schedule(1000);
    }

    private static void performDocumentLookup( String documentId )
    {

        consoleLog("looking for " + documentId);

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

            Label feedbackLabel =  (Label) VisualisationPanel.resultsFeedbackPanel.getWidget(0);

            feedbackLabel.setText("I am here");

            //we'll populate the entire window with letters here
            CommonFormatter.formatLetterDisplay(result, lettersContainer);

            if (DOM.getElementById("li_1") != null)
            {
                ImageElement loaderImage = (ImageElement) DOM.getElementById("li_1").cast();

                loaderImage.getStyle().setVisibility(Style.Visibility.HIDDEN);
            }

            buildScatterPlot("#div_for_svg", result.getSelectedStats(), false);
        }

        public void onFailure(Throwable throwable)
        {

        }

        // call chronology.js to create Chronology Chart
        private native void buildScatterPlot(String div, String datString, boolean replace)/*-{
            $wnd.buildScatterPlotChart(div, datString, replace);
        }-*/;
    }

    native static void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;


}

