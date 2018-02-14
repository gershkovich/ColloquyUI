package us.colloquy.tolstoy.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.async.LoadVisualisationChart;
import us.colloquy.tolstoy.client.async.SearchAdditionalLettersAsynchCallback;
import us.colloquy.tolstoy.client.async.SearchMaterialAsynchCallback;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class VisualisationPanel extends Composite
{

    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);
    //status information
    private Label feedback = new Label("Stats");

    private TextBox searchTextBox;

    private final Hidden numberOfLoadedLetters;

    public final static VerticalPanel lettersContainer = new VerticalPanel();

    private final Image loadingProgressImage;

    public static final SearchFacets searchFacets = new SearchFacets();

    private final CheckBox letterCheckbox = new CheckBox(constants.lettersCheckboxLabel());

    private final CheckBox diariesCheckbox = new CheckBox(constants.diariesCheckboxLabel());

    public VisualisationPanel(TextBox searchTextBoxIn, Hidden numberOfLoadedLettersIn, Image loadingProgressImageIn, SearchFacets searchFacetsIn)
    {
        searchTextBox = searchTextBoxIn;

        numberOfLoadedLetters = numberOfLoadedLettersIn;

        loadingProgressImage = loadingProgressImageIn;

        searchFacets.getIndexesList().clear();

        searchFacets.getIndexesList().addAll(searchFacetsIn.getIndexesList());

        final ScrollPanel letters = new ScrollPanel();

        lettersContainer.getElement().setId("letter_content_1");

        SplitLayoutPanel slp = new SplitLayoutPanel(3)
        {
            @Override
            public void onResize()
            {
                super.onResize();

                resizeLetterArea();

            }

            private void resizeLetterArea()
            {
                //find position of all elements
                if (this.getWidgetCount() > 0)
                {
                    Iterator<Widget> iter = this.getChildren().iterator();

                    //HSplitter has left and write - take write = left on scroll panel
                    //VSplitter - take right = right on scroll panel
                    //VSplitter - take bottom = top on scroll panel
                    //HSplitter - take bottom = bottom on scroll panel

                    int left = 0;
                    int right = 0;
                    int top = 0;
                    int bottom = 0;

                    while (iter.hasNext())
                    {
                        Widget w = iter.next();

                        if (w.getClass().getName().contains("HSplitter"))
                        {

                            left = w.getElement().getAbsoluteRight();
                            bottom = w.getElement().getAbsoluteBottom();
                        } else if (w.getClass().getName().contains("VSplitter"))
                        {

                            right = w.getElement().getAbsoluteRight();
                            top = w.getElement().getAbsoluteBottom();
                        }
                    }
                    //set ScrollPanel position
                    letters.setSize(right - left + "px", bottom - top + "px");
                }
            }
        };

        HorizontalPanel chronologyPanel = new HorizontalPanel();

        VerticalPanel facetVerticalPanel = new VerticalPanel();

        HorizontalPanel letterHorizontalPanel = new HorizontalPanel();

        HorizontalPanel diariesHorizontalPanel = new HorizontalPanel();


        letterCheckbox.setStyleName("checkBoxSmallLabel");
        diariesCheckbox.setStyleName("checkBoxSmallLabel");
        letterCheckbox.setValue(true);
        diariesCheckbox.setValue(true);

        letterHorizontalPanel.add(letterCheckbox);

        letterHorizontalPanel.add(diariesCheckbox);

        letterCheckbox.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                facetedSearch();
            }
        });

        diariesCheckbox.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                facetedSearch();
            }
        });


//        Label diariesLabel = new Label(constants.diariesCheckboxLabel());
//
//        Label lettersLabel = new Label();

//        diariesLabel.setStyleName("titleSmallLabel");
//        lettersLabel.setStyleName("titleSmallLabel");

        facetVerticalPanel.add(letterHorizontalPanel);
        facetVerticalPanel.add(diariesHorizontalPanel);

        slp.addWest(facetVerticalPanel, Window.getClientWidth() / 7.8);
        slp.addNorth(chronologyPanel, Window.getClientHeight() / 3.8);


        slp.addStyleName("splitLayoutPanel");

        letters.setSize(Window.getClientWidth() / 6 * 5 + "px", (Window.getClientHeight() / 4 * 3 - 90) + "px");

        letters.add(lettersContainer);

        slp.add(letters);

        //add here all widgets to resize
        Window.addResizeHandler(new ResizeHandler()
        {

            Timer resizeTimer = new Timer()
            {
                @Override
                public void run()
                {

                    slp.onResize(); //it is enough to call that method to resize internal elements

                }
            };

            @Override
            public void onResize(ResizeEvent event)
            {
                resizeTimer.cancel();
                resizeTimer.schedule(100);
            }
        });

        letters.addScrollHandler(new ScrollHandler()
        {
            @Override
            public void onScroll(ScrollEvent event)
            {
                //on scroll down to the bottom add more records
                int max = letters.getMaximumVerticalScrollPosition();
                int pos = letters.getVerticalScrollPosition();

                int total = 0;
                int loaded = -1;

                String[] recordsCounts = feedback.getText().split(",");

                if (recordsCounts.length == 2)
                {

                    String totalS = recordsCounts[0].replaceAll("\\D", "").trim();
                    String loadedS = recordsCounts[1].replaceAll("\\D", "").trim();

                    if (totalS.matches("\\d{1,10}") && loadedS.matches("\\d{1,10}"))
                    {
                        total = Integer.valueOf(totalS);
                        loaded = Integer.valueOf(loadedS);
                    }
                }

                if ((max - pos) < 20 && total != loaded)
                {
                    loadingProgressImage.setVisible(true);

                    int totalLettersLoadedOnClient = Integer.valueOf(numberOfLoadedLetters.getValue());

                    SearchFacets searchFacets = new SearchFacets();

                    //collect info into search facet

                    //load more records
                    TolstoyService.App.getInstance().getSelectedLettersWithOffset(totalLettersLoadedOnClient, searchTextBox.getText(), searchFacets,
                            new SearchAdditionalLettersAsynchCallback(lettersContainer, feedback, numberOfLoadedLetters, loadingProgressImage));
                }
            }
        });


        DivElement div = Document.get().createDivElement();
        div.setId("div_for_svg");
        div.setClassName("svg-container");

        //  chronologyPanel.add(feedback);
        HorizontalPanel chart = new HorizontalPanel();
        chronologyPanel.add(chart);

        chart.getElement().appendChild(div);


        initWidget(slp);

    }


    private void facetedSearch()
    {
        loadingProgressImage.setVisible(false);

        searchFacets.getIndexesList().clear();

        lettersContainer.clear();

        //gif - loading

        
        if (letterCheckbox.getValue())
        {
            searchFacets.getIndexesList().add("tolstoy_letters");
        }

        if (diariesCheckbox.getValue())
        {
            searchFacets.getIndexesList().add("tolstoy_diaries");
        }

        if (diariesCheckbox.getValue() || letterCheckbox.getValue())
        {
            loadingProgressImage.setVisible(true);
            //do search with text the content of text box;
            //this call will load the diagram first and then load records via DataUplink class
            TolstoyService.App.getInstance().getDataForCharts( searchFacets, new UpdateVisualisationChart());

//            //load more records
//            TolstoyService.App.getInstance().getSelectedLettersWithOffset(0, searchTextBox.getText(), searchFacets,
//                    new SearchAdditionalLettersAsynchCallback(lettersContainer, feedback, numberOfLoadedLetters, loadingProgressImage));
        }
    }


    public void createVisualization(String csvLettersData, String documentType)
    {
        Document.get().getElementById("div_for_svg").removeAllChildren();


        buildChronology("#div_for_svg", csvLettersData, documentType);
    }


    public void resetProperties()
    {
        //todo should potentially load user properties if available


    }

    public VerticalPanel getLettersContainer()
    {
        return lettersContainer;
    }

    public Label getFeedback()
    {
        return feedback;
    }

    public void setFeedback(Label feedback)
    {
        this.feedback = feedback;
    }

    public Hidden getNumberOfLoadedLetters()
    {
        return numberOfLoadedLetters;
    }


    // call chronology.js to create Chronology Chart
    private native void buildChronology(String div, String datString, String documentType)/*-{
        $wnd.buildChronologyChart(div, datString, documentType);
    }-*/;

    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

    class UpdateVisualisationChart implements AsyncCallback<ServerResponse>
    {
        private TolstoyMessages messages = GWT.create(TolstoyMessages.class);

        public UpdateVisualisationChart()
        {

        }

        @Override
        public void onFailure(Throwable caught)
        {
            getFeedback().setText(constants.retrievalError());

        }

        @Override
        public void onSuccess(ServerResponse result)
        {
            String documentType = constants.documentTerm();

            if (searchFacets.getIndexesList().size() == 1)
            {
                if ("tolstoy_diaries".equalsIgnoreCase(searchFacets.getIndexesList().get(0)))
                {
                    documentType = constants.diariesCheckboxLabel();//same
                }   else
                {
                    documentType = constants.lettersCheckboxLabel();//same
                }
            }

            createVisualization(result.getCsvLetterData(), documentType);
        }
    }

}
