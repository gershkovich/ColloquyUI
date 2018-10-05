package us.colloquy.tolstoy.client.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.model.LetterDisplay;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;
import us.colloquy.tolstoy.client.util.CommonFormatter;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class SearchMaterialAsynchCallback  implements AsyncCallback<ServerResponse>
{
    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);

    private VisualisationPanel vp;

    private Image loadingProgressImage;

    public SearchMaterialAsynchCallback(VisualisationPanel vpIn, Image loadingProgressImageIn)
    {
        vp = vpIn;
        loadingProgressImage=loadingProgressImageIn;

    }

    @Override
    public void onFailure(Throwable caught)
    {
        loadingProgressImage.setVisible(false);

        vp.getFeedback().setText(constants.retrievalError());
    }

    @Override
    public void onSuccess(ServerResponse result)
    {
        loadingProgressImage.setVisible(false);

        vp.getLettersContainer().clear();

        CommonFormatter.formatLetterDisplay(result, vp.getLettersContainer());

        vp.getNumberOfLoadedLetters().setValue(result.getLetters().size() + "");

        vp.getFeedback().setText(messages.numberOfLetterFound(result.getTotalNumberOfLetters() + "", vp.getNumberOfLoadedLetters().getValue()));

        buildScatterPlot("#div_for_svg", result.getSelectedStats(), true);

    }

    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

    // call chronology.js to create Chronology Chart
    private native void buildScatterPlot(String div, String datString, boolean replace)/*-{
        $wnd.buildScatterPlotChart(div, datString, replace);
    }-*/;
}
