package us.colloquy.tolstoy.client.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
        vp.getFeedback().setText(constants.retrievalError());

    }

    @Override
    public void onSuccess(ServerResponse result)
    {
        String documentType = constants.documentTerm();

        if (VisualisationPanel.searchFacets.getIndexesList().size() == 1)
        {
            if ("tolstoy_diaries".equalsIgnoreCase(VisualisationPanel.searchFacets.getIndexesList().get(0)))
            {
                documentType = constants.diariesCheckboxLabel();//same
            }   else
            {
                documentType = constants.lettersCheckboxLabel();//same
            }
        }

        vp.createVisualization(result.getCsvLetterData(), documentType);
    }
}
