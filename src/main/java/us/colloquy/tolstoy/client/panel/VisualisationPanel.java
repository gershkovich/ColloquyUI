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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import us.colloquy.tolstoy.client.TolstoyConstants;
import us.colloquy.tolstoy.client.TolstoyMessages;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.async.SearchAdditionalLettersAsynchCallback;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;

import java.util.Iterator;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class VisualisationPanel extends Composite
{

    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);
    //status information
    private Label feedback = new Label(constants.resultsLabel());

    private TextBox searchTextBox ;

    public static final HorizontalPanel resultsFeedbackPanel = new HorizontalPanel();

    private final Hidden numberOfLoadedLetters;

    public final static VerticalPanel lettersContainer = new VerticalPanel();

    private final Image loadingProgressImage;

    public static final SearchFacets searchFacets = new SearchFacets();

    private final CheckBox letterCheckbox = new CheckBox(constants.lettersCheckboxLabel());

    private final CheckBox diariesCheckbox = new CheckBox(constants.diariesCheckboxLabel());

    public VisualisationPanel(TextBox searchTextBoxIn, Hidden numberOfLoadedLettersIn, Image loadingProgressImageIn, SearchFacets searchFacetsIn, String localeIn)
    {
        searchTextBox = searchTextBoxIn;

        numberOfLoadedLetters = numberOfLoadedLettersIn;

        loadingProgressImage = loadingProgressImageIn;

        searchFacets.getIndexesList().clear();

        searchFacets.getIndexesList().addAll(searchFacetsIn.getIndexesList());

        final ScrollPanel lettersScroll = new ScrollPanel();

        final ScrollPanel chartScroll = new ScrollPanel();

        lettersContainer.getElement().setId("letter_content_1");

        HorizontalPanel chart = new HorizontalPanel();

        DivElement div = Document.get().createDivElement();

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
                    //Widget top:" +
                    int panelTop = this.getElement().getAbsoluteTop();


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
//                    consoleLog("Widget top:" + this.getElement().getAbsoluteTop() + "; Right:" + right + "; Top:" + top + "; Bottom:" + bottom);
//                    consoleLog("Left:" + left + "; Right:" + right + "; Top:" + top + "; Bottom:" + bottom);

                    //set ScrollPanel position
                    lettersScroll.setSize(right - left + "px", bottom - top + "px");

                    chartScroll.setSize(right - left + "px",  top - panelTop + "px");
                    chartScroll.setVerticalScrollPosition(chartScroll.getMaximumVerticalScrollPosition());

                }
            }
        };

        VerticalPanel chronologyPanel = new VerticalPanel();

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
        slp.addNorth(chronologyPanel, 410);

        slp.addStyleName("splitLayoutPanel");

        lettersScroll.setSize(Window.getClientWidth() / 6 * 5 + "px", (Window.getClientHeight() / 4 * 3 - 90) + "px");
        chartScroll.setSize( Window.getClientWidth() / 6 * 5 + "px",  "400px");

      consoleLog(Window.getClientHeight() + " height");
        lettersScroll.add(lettersContainer);

        lettersScroll.setStyleName("scrollPanel_smooth");

        slp.add(lettersScroll);

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

        lettersScroll.addScrollHandler(new ScrollHandler()
        {
            @Override
            public void onScroll(ScrollEvent event)
            {
                //on scroll down to the bottom add more records
                int max = lettersScroll.getMaximumVerticalScrollPosition();
                int pos = lettersScroll.getVerticalScrollPosition();

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
                     //todo move that to on click
                    //load more records
                    TolstoyService.App.getInstance().getSelectedLettersWithOffset(totalLettersLoadedOnClient, searchTextBox.getText(), searchFacets,
                            new SearchAdditionalLettersAsynchCallback(lettersContainer, feedback, numberOfLoadedLetters, loadingProgressImage));
                }
            }
        });

        
        div.setId("div_for_svg");
        div.setClassName("svg-container");



        resultsFeedbackPanel.add(feedback);

        feedback.setStyleName("feedback_disabled");

        resultsFeedbackPanel.setStyleName("feedback_panel");

        chronologyPanel.add(resultsFeedbackPanel);

        chronologyPanel.setStyleName("chronology_panel");

        chart.getElement().appendChild(div);
        
      chartScroll.add(chart);
        chronologyPanel.add(chartScroll);

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
            TolstoyService.App.getInstance().getDataForCharts( searchFacets, new UpdateVisualisationChart(searchTextBox, numberOfLoadedLetters));

//            //load more records
//            TolstoyService.App.getInstance().getSelectedLettersWithOffset(0, searchTextBox.getText(), searchFacets,
//                    new SearchAdditionalLettersAsynchCallback(lettersContainer, feedback, numberOfLoadedLetters, loadingProgressImage));
        }
    }


    public void createVisualization(String csvLettersData, String allEvents, String documentType, String [] startAndEndDates)
    {
        Document.get().getElementById("div_for_svg").removeAllChildren();

        buildChronology("#div_for_svg", csvLettersData, allEvents, documentType, startAndEndDates, LocaleInfo.getCurrentLocale().getLocaleName());
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
    private native void buildChronology(String div, String datString, String allEvents, String documentType, String [] startAndEndDates, String location)/*-{

        $wnd.buildChronologyChart(div, datString, allEvents, documentType, startAndEndDates, location);
    }-*/;

    // call chronology.js to create Chronology Chart
    private native void buildScatterPlot(String div, String datString, boolean replace)/*-{
        $wnd.buildScatterPlotChart(div, datString, replace);
    }-*/;

    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

    class UpdateVisualisationChart implements AsyncCallback<ServerResponse>
    {
        private TolstoyMessages messages = GWT.create(TolstoyMessages.class);

        Hidden totalNumberOfLoadedLetters;
        TextBox searchTextBox;

        public UpdateVisualisationChart(TextBox searchTextBoxIn, Hidden totalNumberOfLoadedLettersIn)
        {
            totalNumberOfLoadedLetters=totalNumberOfLoadedLettersIn;

            searchTextBox = searchTextBoxIn;

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

            feedback.setText( messages.numberOfLetterFound(result.getTotalNumberOfLetters() + "", totalNumberOfLoadedLetters.getValue()));

            createVisualization(result.getCsvLetterData(), result.getWorkEvents(), documentType, result.getStartAndEndDates());

            //that just clears scatterplot because there is no result back
           // buildScatterPlot("#div_for_svg", result.getSelectedStats(), true);

            //in theory now we need another search based on the status of
            if (searchTextBox.getText().length() > 0 )
            {
                consoleLog("has text");

                //now we need to run a search based on dates and ranges to apply facets
                

            }
        }
    }

}
