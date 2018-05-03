package us.colloquy.tolstoy.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.async.SearchMaterialAsynchCallback;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.ui.dialogBoxImproved.DialogBoxImproved;

import java.util.*;

/**
 * Created by Peter Gershkovich on 1/7/18.
 */
public class SearchExamplesPopUp
{

    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);

    private DialogBoxImproved dialogBox = new DialogBoxImproved();

    private VerticalPanel mainPanel = new VerticalPanel();

    private TextBox searchElastic;

    private VisualisationPanel vp;

    private Image loadingProgressImage;

    public SearchExamplesPopUp(TextBox searchElasticIn, VisualisationPanel vpIn, Image loadingProgressImageIn)
    {
        loadingProgressImage = loadingProgressImageIn;

        vp = vpIn;

        searchElastic = searchElasticIn;

        mainPanel.setSize(Window.getClientWidth()/3*2 - 25 + "px", Window.getClientHeight()/3*2 - 50 + "px");

        Window.addResizeHandler(new ResizeHandler()
        {

            @Override
            public void onResize(ResizeEvent event)
            {

                mainPanel.setSize(Window.getClientWidth()/3*2 - 25 + "px", Window.getClientHeight()/3*2 - 50 + "px");
            }
        });

        dialogBox.setWindowCloseButtonEnabled(true);

        dialogBox.setModal(true);

        dialogBox.setGlassEnabled(true);

        dialogBox.setAnimationEnabled(true);

        dialogBox.setHTML("<span class=\"header_label\"><b>" + constants.searchExamplesWindowHeader() + "</b></span>");

        dialogBox.setWidget(mainPanel);
    }

    public void buildLoadingMessage()
    {
        mainPanel.clear();

        Image loadingImage = new Image("images/ajax-loader-2.gif");

        VerticalPanel innerPanel = new VerticalPanel();

        innerPanel.add(loadingImage);

        mainPanel.add(innerPanel);
    }

    public void show()
    {

        dialogBox.center();
    }

    public void setErrorMessage(String errorMessageIn)
    {

        mainPanel.clear();

    }

    public void build()
    {

        mainPanel.clear();

        mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

        dialogBox.setModal(false);

        dialogBox.setAnimationEnabled(true);


        VerticalPanel innerMainPanel = new VerticalPanel();

        // dialogBox.seth("<b>"+ constants.getSearchExamplesTitle() + "</b>");

          dialogBox.setWindowCloseButtonEnabled(true);

        //dialogBox.addStyleName("example_dialog");

        dialogBox.setWidth(Window.getClientWidth() / 2 + "px");

        ScrollPanel scrollPanelForExamples = new ScrollPanel();

        scrollPanelForExamples.setSize(Window.getClientWidth()/3*2 - 25 + "px", Window.getClientHeight()/3*2 - 50 + "px");
        

        VerticalPanel linksPanel = new VerticalPanel();


        HTML generalSearchPrinciple = new HTML(constants.getGeneralSearchPrincipleLabel());

        generalSearchPrinciple.setStyleName("interfaceLabelInstructions");

        linksPanel.add(generalSearchPrinciple);

        List<String[]> examples = new ArrayList<String[]>();

        examples.add(new String[]{constants.searchExampleOne(), constants.searchExampleOneExp()});
        examples.add(new String[]{constants.searchExampleTwo(), constants.searchExampleTwoExp()});
        examples.add(new String[]{constants.searchExampleThree(), constants.searchExampleThreeExp()});
        examples.add(new String[]{constants.searchExampleFour(), constants.searchExampleFourExp()});
        examples.add(new String[]{constants.searchExampleFive(), constants.searchExampleFiveExp()});


        for (String[] ex : examples)
        {
            VerticalPanel anchorPanel = new VerticalPanel();

            final Anchor anchor = new Anchor(ex[0]);

            InlineLabel label = new InlineLabel(ex[1]);

            label.setStyleName("exampleLabel");

            anchor.setStyleName("exampleAnchor");

            anchorPanel.setStyleName("exampleAnchor");

            anchor.setWordWrap(false);

            anchor.setTitle("Click on the link to run the example.");

            anchor.addClickHandler(new ClickHandler()
            {

                @Override
                public void onClick(ClickEvent event)
                {
                    searchElastic.setText(anchor.getText());

                    dialogBox.hide(true);

                    searchElastic.setFocus(true);

                    loadingProgressImage.setVisible(true);

                    SearchFacets searchFacets = new SearchFacets();

                    //collect info into search facet

                    TolstoyService.App.getInstance().getSelectedLetters(searchElastic.getText(), searchFacets,
                            new SearchMaterialAsynchCallback(vp, loadingProgressImage));


                }
            });

            anchorPanel.add(anchor);
            anchorPanel.add(label);

            linksPanel.add(anchorPanel);

        }

        //see details at http://www.lucenetutorial.com/lucene-query-syntax.html

        scrollPanelForExamples.setWidget(linksPanel);

        innerMainPanel.add(scrollPanelForExamples);

        mainPanel.add(innerMainPanel);

        dialogBox.setWidget(mainPanel);

        dialogBox.center();

    }


    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

}
