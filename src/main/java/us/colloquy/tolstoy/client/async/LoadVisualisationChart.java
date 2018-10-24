package us.colloquy.tolstoy.client.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;

/**
 * Created by Peter Gershkovich on 12/27/17.
 */
public class LoadVisualisationChart implements AsyncCallback<ServerResponse>
{
    VisualisationPanel vp;

    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);


    public LoadVisualisationChart(VisualisationPanel vpIn)
    {

        vp = vpIn;

    }

    @Override
    public void onFailure(Throwable caught)
    {
        Label feedbackLabel =  (Label) VisualisationPanel.resultsFeedbackPanel.getWidget(0);

        feedbackLabel.setText(constants.retrievalError());

    }

    @Override
    public void onSuccess(ServerResponse result)
    {
        String [] yAxisLabels = new String [3];

        String documentType = constants.documentsLabel();

        if (VisualisationPanel.searchFacets.getIndexesList().size() == 1)
        {
            if ("tolstoy_diaries".equalsIgnoreCase(VisualisationPanel.searchFacets.getIndexesList().get(0)))
            {
                documentType = constants.diariesLabel();

            }   else
            {
                documentType = constants.lettersLabel();
            }
        }

        String [] periods = constants.scaleТimePeriod().split(",");

        for (int i = 0; i < periods.length; i++ )
        {
            yAxisLabels[i] = documentType + " " + periods[i];

        }

        vp.createVisualization(result.getCsvLetterData(), result.getWorkEvents(), yAxisLabels, result.getStartAndEndDates());
    }

    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;
}
