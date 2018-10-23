package us.colloquy.tolstoy.client.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import us.colloquy.tolstoy.client.Tolstoy;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.model.LetterDisplay;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;
import us.colloquy.tolstoy.client.util.CommonFormatter;

/**
 * Created by Peter Gershkovich on 12/25/17.
 */
public class SearchAdditionalLettersAsynchCallback implements AsyncCallback<ServerResponse>
{
    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);


    VerticalPanel lettersContainer;

    Image loadingProgressImage;

    public SearchAdditionalLettersAsynchCallback(VerticalPanel lettersContainerIn,  Image loadingProgressImageIn)
    {

        lettersContainer = lettersContainerIn;

        loadingProgressImage = loadingProgressImageIn;
    }

    @Override
    public void onFailure(Throwable caught)
    {
        Label feedbackLabel =  (Label) VisualisationPanel.resultsFeedbackPanel.getWidget(0);

        feedbackLabel.setText(constants.retrievalError());
        loadingProgressImage.setVisible(false);

        Tolstoy.loadInProgress.setValue("false");  //also finish loading if error
    }

    @Override
    public void onSuccess(ServerResponse result)
    {
        loadingProgressImage.setVisible(false);
        //get total number of records

        //note that here we are not deleting any letters just adding them
        CommonFormatter.formatLetterDisplay(result, lettersContainer);

        int numberOfPreviouslyLoadedDocuments = Integer.valueOf(Tolstoy.numberOfLoadedLetters.getValue());

        consoleLog("fond " + result.getLetters().size());

        consoleLog("loading additional letters - previously loaded: " + numberOfPreviouslyLoadedDocuments);

        Tolstoy.numberOfLoadedLetters.setValue((numberOfPreviouslyLoadedDocuments + result.getLetters().size()) + "");

        consoleLog("searching for additional letters setting to " + Tolstoy.numberOfLoadedLetters.getValue() + " of total " +   Tolstoy.totalNumberOfLetters.getValue());

        Label feedbackLabel =  (Label) VisualisationPanel.resultsFeedbackPanel.getWidget(0);

        feedbackLabel.setText(messages.numberOfLetterFound( Tolstoy.numberOfLoadedLetters.getValue(),  Tolstoy.totalNumberOfLetters.getValue() ));

        buildScatterPlot("#div_for_svg", result.getSelectedStats(), false);

        Tolstoy.loadInProgress.setValue("false");  //finished loading
    }

    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

    // call chronology.js to create Chronology Chart
    private native void buildScatterPlot(String div, String datString, boolean replace)/*-{
        $wnd.buildScatterPlotChart(div, datString, replace);
    }-*/;
}
