package us.colloquy.tolstoy.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import us.colloquy.tolstoy.client.async.LoadVisualisationChart;
import us.colloquy.tolstoy.client.async.SearchMaterialAsynchCallback;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.panel.SearchExamplesPopUp;
import us.colloquy.tolstoy.client.panel.VisualisationPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Tolstoy implements EntryPoint, ValueChangeHandler<String>
{
    private static final TolstoyConstants constants = GWT.create(TolstoyConstants.class);

    private TolstoyMessages messages = GWT.create(TolstoyMessages.class);

    private final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);   //overall panel

    private final VerticalPanel contentPanel = new VerticalPanel();

    private final VerticalPanel mainCentralVerticalPanel = new VerticalPanel();

    private final SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();

    private final Hyperlink descriptionLink = new Hyperlink(constants.about(), "show");

    private final List<Hyperlink> menuItems = new ArrayList<>();

    private final SearchFacets searchFacets = new SearchFacets();

    private Hyperlink menuLink = new Hyperlink("", "menuItems");

    private Hyperlink navHome = new Hyperlink();
    private Hyperlink navIndex = new Hyperlink();
    private Hyperlink navSearch = new Hyperlink();
    private Hyperlink navTechnical = new Hyperlink();
    private Hyperlink navComment = new Hyperlink();
    private Image menuIcon = new Image("menuIconActive.png");

    private ScrollPanel mainContentScroll = new ScrollPanel();

    private final FlowPanel flowPanel = new FlowPanel();

    private final FlowPanel flowPanelMain = new FlowPanel();

    private final  HorizontalPanel titlePanel = new HorizontalPanel();

    private final Label smallTitle = new Label();

    private final Image loadingProgressImage = new Image();

    HorizontalPanel miscNavigation = new HorizontalPanel();

    public final static TextBox searchElastic = new TextBox();


    public void onModuleLoad()
    {

        Window.setTitle(constants.projectTitle());

        HorizontalPanel titleBar = new HorizontalPanel();

        titleBar.setStyleName("titleBar");

        titleBar.setWidth("100%");

        dockLayoutPanel.addNorth(titleBar, 5);

        String localeName = LocaleInfo.getCurrentLocale().getLocaleName();

        VerticalPanel entireVerticalTitlePanel = new VerticalPanel();

        //set small title that will be initially invisible it will be easy to change it if need bigger screen space

         smallTitle.setText(constants.projectTitle());

         miscNavigation.add(smallTitle);
         
         smallTitle.setStyleName("titleSmallLabel");

         smallTitle.setVisible(false);

        loadingProgressImage.setUrl( "images/ajax-loader.gif" );

        loadingProgressImage.getElement().setId("li_1");

        loadingProgressImage.setVisible(false);

        loadingProgressImage.setStyleName("loadingProgressImage");

        //Set language navigation
        setLangNavigation(localeName, entireVerticalTitlePanel);

        entireVerticalTitlePanel.setStyleName("title");

        addAppTitle(); //sets content of tilePanel

        entireVerticalTitlePanel.add(titlePanel);

        entireVerticalTitlePanel.setCellVerticalAlignment(titlePanel, HasVerticalAlignment.ALIGN_BOTTOM);

        menuIcon.addStyleName("menuIcon");

        menuLink.getElement().appendChild(menuIcon.getElement());

        menuIcon.setTitle(constants.menuText());

        titlePanel.setWidth("100%");
        titlePanel.setHeight("100%");


        titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);
        titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

        titleBar.add(entireVerticalTitlePanel);

        dockLayoutPanel.addEast(contentPanel, 40);

        dockLayoutPanel.setWidgetHidden(contentPanel, true);

        //done with tile section

        setInitialCentralPanelLayout();

        //build central menu panel
        setMainCentralMenuPanel();

        //todo replace central panel content and background on menu change


//        navAboutPanel.setStyleName(css.navigationItemPanel());
//        navIndexPanel.setStyleName(css.navigationItemPanel());
//        navTechnicalPanel.setStyleName(css.navigationItemPanel());
//        navCommentPanel.setStyleName(css.navigationItemPanel());
//        //    navAuthorPanel.setStyleName(css.navigationItemPanel());


//
//        navAboutPanel.add(navAbout);
//        navIndexPanel.add(navIndex);
//        navTechnicalPanel.add(navTechnical);
//        navCommentPanel.add(navComment);
        //    navAuthorPanel.add(navAuthor);
        // If the application starts with no history token, redirect to a new
        // 'baz' state.


        // Add widgets to the root panel.
//        VerticalPanel navigationPanel = new VerticalPanel();
//        navigationPanel.setStyleName(css.navigationPanel());
//
//
//        // navigationPanel.add(lbl);
//        navigationPanel.add(navAboutPanel);
//        navigationPanel.add(navIndexPanel);
//        navigationPanel.add(navTechnicalPanel);
//        navigationPanel.add(navCommentPanel);
        //   navigationPanel.add(navAuthorPanel);


        mainPanel.setWidth("100%");

        mainPanel.setWidget(mainCentralVerticalPanel);

        dockLayoutPanel.add(mainPanel);

        //  VerticalPanel panel = new VerticalPanel();
        flowPanel.addStyleName("flow");

        final ScrollPanel scrollPanel = new ScrollPanel();

        scrollPanel.setWidth("100%");


       // scrollPanel.setAlwaysShowScrollBars(true);

        contentPanel.setHeight("100%");
        contentPanel.add(scrollPanel);

        setAbout();
        scrollPanel.add(flowPanel);

        scrollPanel.addStyleName("scrollPanel");



        //  rp.getWidgetContainerElement(layoutPanel).getStyle().setOverflowY(Style.Overflow.AUTO);

        /*
        <p>
    Tolstoy was an avid reader who drew on a multitude of philosophical and scientific sources in his own literary and theoretical works. There is a wealth of information regarding what Tolstoy read, when he read it, and whom he spoke to about it. Tolstoy himself chronicled such things in his journals and kept up a vigorous correspondence with his many intellectual acquaintances. But this information is difficult to access: it is dispersed across collected volumes of letters, diaries, and secondary sources such as The Chronicle of the Life and Art of L.N. Tolstoy, written by Tolstoy’s personal secretary. This project would make this information accessible not only to scholars pursuing specialized research but also to students and casual readers of Tolstoy. Together with my colleague at Harvard, I propose to collate and digitize these many disparate sources and create an interface that would let users search and analyze the material according to different parameters (dates, events, names). With the Seed Grant, we would undertake a proof-of-concept project, building a prototype for such a database and interface.
</p>

<p>Explore Tolstoy's letters using Kibana - <a></a></p>
         */

        // Assume that the host HTML has elements defined whose
        // IDs are "slot1", "slot2".  In a real app, you probably would not want
        // to hard-code IDs.  Instead, you could, for example, search for all
        // elements with a particular CSS class and replace them with widgets.
        //
//        RootPanel.get("slot1").add(button);
//        RootPanel.get("slot2").add(label);

       // flowPanelMain.setHeight((Window.getClientHeight()) + "px");

        scrollPanel.setHeight((Window.getClientHeight() - 80) + "px");

        Window.addResizeHandler(new ResizeHandler() {

            Timer resizeTimer = new Timer() {
                @Override
                public void run() {

                    mainContentScroll.setHeight((Window.getClientHeight() - 110) + "px");
                   // flowPanelMain.setHeight((Window.getClientHeight()) + "px");
                    scrollPanel.setHeight((Window.getClientHeight() - 80) + "px");

                }
            };

            @Override
            public void onResize(ResizeEvent event) {
                resizeTimer.cancel();
                resizeTimer.schedule(250);
            }
        });

        RootLayoutPanel rp = RootLayoutPanel.get();

        rp.add(dockLayoutPanel);

        // Add history listener
        History.addValueChangeHandler(this);

        String initToken = History.getToken();

        if (initToken.length() == 0)
        {
            History.newItem("home");
        }

        // Now that we've setup our listener, fire the initial history state.
        History.fireCurrentHistoryState();

    }

    private void addAppTitle()
    {
        Label projectTitle = new Label(constants.projectTitle());

        projectTitle.addStyleName("titleLabel");

        titlePanel.add(projectTitle);
        titlePanel.setCellVerticalAlignment(projectTitle, HasVerticalAlignment.ALIGN_MIDDLE);

        titlePanel.setCellHorizontalAlignment(projectTitle, HasHorizontalAlignment.ALIGN_LEFT);


    }

    private void setMainCentralMenuPanel()
    {
        navHome.setText(constants.home());
        navHome.setTargetHistoryToken("home");

        navIndex.setText(constants.index());
        navIndex.setTargetHistoryToken("introduction");

        navSearch.setText(constants.search());
        navSearch.setTargetHistoryToken("search");

        navTechnical.setText(constants.technicalOverview());
        navTechnical.setTargetHistoryToken("technology");

        navComment.setText(constants.comments());
        navComment.setTargetHistoryToken("comments");

        menuItems.add(navHome);
        menuItems.add(navIndex);
        menuItems.add(navSearch);
        menuItems.add(navTechnical);
        menuItems.add(navComment);



        // mainCentralVerticalPanel.setHeight("100%");

//        mainContentScroll.setWidth("100%");
//        mainContentScroll.setHeight("100%");

        HorizontalPanel menuContainer = new HorizontalPanel();

        menuContainer.setWidth("70%");

        menuContainer.addStyleName("layoutStyleMenu");

        menuContainer.add(navHome);
        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navIndex);
        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navSearch);
        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navTechnical);
        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navComment);
        menuContainer.add(new HTML("&nbsp;"));

        menuContainer.setCellHorizontalAlignment(navHome, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navIndex, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navSearch, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navTechnical, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navComment, HasHorizontalAlignment.ALIGN_LEFT);

        titlePanel.add(menuContainer);

        titlePanel.setCellVerticalAlignment(menuContainer, HasVerticalAlignment.ALIGN_MIDDLE);


        mainContentScroll.setWidget(flowPanelMain);
        mainContentScroll.setAlwaysShowScrollBars(true);


        mainContentScroll.setHeight((Window.getClientHeight() - 110) + "px");

        manageMenuStyles("home");

    }

    private void setInitialCentralPanelLayout()
    {
        Label projectSubTitle = new Label(constants.projectTitle2());

        projectSubTitle.setStyleName("subtitle");

        mainCentralVerticalPanel.addStyleName("layoutStyle");

        HorizontalPanel redArrowPanel = new HorizontalPanel();

        redArrowPanel.setWidth("100%");

        redArrowPanel.add(projectSubTitle);

        descriptionLink.addStyleName("arrorNav");

        Image redArrowImage = new Image("navig-arr.png");

        redArrowImage.addStyleName("redArrImage");

        descriptionLink.getElement().appendChild(redArrowImage.getElement());

        redArrowPanel.add(descriptionLink);

        mainCentralVerticalPanel.add(redArrowPanel);

        redArrowPanel.setCellHorizontalAlignment(projectSubTitle, HasHorizontalAlignment.ALIGN_LEFT);
        redArrowPanel.setCellHorizontalAlignment(descriptionLink, HasHorizontalAlignment.ALIGN_RIGHT);
        redArrowPanel.setCellVerticalAlignment(descriptionLink, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    private void setLangNavigation( String localeName, VerticalPanel entireVerticalTitlePanel)
    {


        HorizontalPanel lang = new HorizontalPanel();
        miscNavigation.add(lang);

        miscNavigation.addStyleName("miscNavig");

        final Anchor eng = new Anchor("Eng");
        final Anchor ru = new Anchor("Ru");

        Label divider = new Label("|");


        divider.setStyleName("divider");
        lang.add(eng);
        lang.add(divider);
        lang.add(ru);
        lang.setStyleName("lang");


        if ("en".equalsIgnoreCase(localeName))
        {
            eng.getElement().getParentElement().setClassName("langSel");
            ru.getElement().getParentElement().setClassName("langUnsel");

        } else if ("ru".equalsIgnoreCase(localeName))
        {
            ru.getElement().getParentElement().setClassName("langSel");
            eng.getElement().getParentElement().setClassName("langUnsel");
        }


        eng.setTitle("English");

        ru.setTitle("Русский");


        eng.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {

                String initToken = History.getToken();

                if (initToken.length() == 0)
                {
                    History.newItem("hide");
                }

                Window.Location.assign(GWT.getHostPageBaseURL() + "Tolstoy.html?locale=en#" + initToken);
                eng.getElement().getParentElement().setClassName("langSel");
                ru.getElement().getParentElement().setClassName("langUnsel");


            }

        });

        ru.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {

                String initToken = History.getToken();

                if (initToken.length() == 0)
                {
                    History.newItem("hide");
                }

                Window.Location.assign(GWT.getHostPageBaseURL() + "Tolstoy.html?locale=ru#" + initToken);
                ru.getElement().getParentElement().setClassName("langSel");
                eng.getElement().getParentElement().setClassName("langUnsel");
            }

        });


        miscNavigation.setCellHorizontalAlignment(lang, HasHorizontalAlignment.ALIGN_RIGHT);


        entireVerticalTitlePanel.add(miscNavigation);
    }

    private static class CommentsAsyncCallback implements AsyncCallback<String>
    {
        private HTML label;

        public CommentsAsyncCallback(HTML label)
        {
            this.label = label;
        }

        public void onSuccess(String result)
        {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();

            if ("success".equalsIgnoreCase(result))
            {
                builder.appendEscaped(constants.emailFeedback());

                label.getElement().setInnerHTML(builder.toSafeHtml().asString());


            } else if ("invalidEmail".equalsIgnoreCase(result))
            {
                builder.appendEscaped(constants.emailFeedback());

                label.getElement().setInnerHTML(builder.toSafeHtml().asString());
                label.getElement().setInnerHTML(constants.emailFeedbackErr2());
            } else
            {
                builder.appendEscaped(constants.emailFeedback());

                label.getElement().setInnerHTML(builder.toSafeHtml().asString());
                label.getElement().setInnerHTML(constants.emailFeedbackErr1());
            }
        }

        public void onFailure(Throwable throwable)
        {
            label.setText("Failed to receive answer from server!");
        }
    }

    public void onValueChange(ValueChangeEvent<String> event)
    {

        // This method is called whenever the application's history changes. Set
        // the label to reflect the current history token.
//        Widget hp = (Widget) event.getSource();
//        hp.addStyleName("navigationItemSel");

        // hp.setStyleName(css.navigationItemSel());

        String value = event.getValue();
        
        consoleLog("event" + value);

        dockLayoutPanel.setWidgetHidden(contentPanel, true);  //hide first

        if ("show".equalsIgnoreCase(value))
        {

            dockLayoutPanel.setWidgetHidden(contentPanel, false);

            descriptionLink.setTargetHistoryToken("hide");


        } else if ("hide".equalsIgnoreCase(value))
        {

            dockLayoutPanel.setWidgetHidden(contentPanel, true);

            descriptionLink.setTargetHistoryToken("show");


//        navAboutPanel.setStyleName(css.navigationItemPanelSelected());

//        setAbout(css);

        } else if ("home".equalsIgnoreCase(value))
        {
            clearStyles();

            titlePanel.clear();

            addAppTitle();

            setMainCentralMenuPanel();

            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

         //   menuLink.setTargetHistoryToken("menuItems");

            mainPanel.setWidget(mainCentralVerticalPanel);

            dockLayoutPanel.setWidgetHidden(contentPanel, true);

            menuIcon.setUrl("menuIconActive.png");

        } else if ("menuItems".equalsIgnoreCase(value))
        {
            titlePanel.clear();

            clearStyles();

            addAppTitle();

            dockLayoutPanel.setWidgetHidden(contentPanel, true);


            mainPanel.setWidget(mainContentScroll);
            menuLink.setTargetHistoryToken("home");
            menuIcon.setUrl("menuIconDefault.png");

            manageMenuStyles("introduction");

            setIndex();

           // titlePanel.add(menuLink);  //todo remove menu link
            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

        } else if ("technology".equalsIgnoreCase(value) )
        {
            setTechnical();
            manageMenuStyles(value);

        } else if ("introduction".equalsIgnoreCase(value))
        {
            //set content
            setIndex();
            manageMenuStyles(value);



        }  else if ("search".equalsIgnoreCase(value))
        {
            clearStyles();

            //menuLink.setTargetHistoryToken("search");
            mainPanel.clear();
          //  visualizationPanel.setSize("100%","100%");
           // visualizationPanel.add(new Label("search"));


            Hidden numberOfloadedLetters = new Hidden();
            numberOfloadedLetters.setDefaultValue("0");
            numberOfloadedLetters.setValue("0");

            //this is default
            searchFacets.getIndexesList().add("tolstoy_letters");
            searchFacets.getIndexesList().add("tolstoy_diaries");

           final  VisualisationPanel vp = new VisualisationPanel(searchElastic, numberOfloadedLetters, loadingProgressImage, searchFacets);

           //make async call to get initial data for visualization

            TolstoyService.App.getInstance().getDataForCharts( searchFacets, new LoadVisualisationChart(vp));

            mainPanel.add(vp);

            searchElastic.setStyleName("seachBox");

            searchElastic.addKeyUpHandler(new KeyUpHandler()
            {
                @Override
                public void onKeyUp(KeyUpEvent event)
                {
                    if ( event.getNativeKeyCode() == KeyCodes.KEY_ENTER )
                    {
                        vp.getLettersContainer().clear();

                        loadingProgressImage.setVisible(true);

                        TolstoyService.App.getInstance().getSelectedLetters(searchElastic.getText(), searchFacets,
                                new SearchMaterialAsynchCallback(vp, loadingProgressImage));
                    }

                }
            });

            titlePanel.clear();

            dockLayoutPanel.setWidgetHidden(contentPanel, true);

            HorizontalPanel searchPanel = new HorizontalPanel();

            VerticalPanel searchExamplePanel = new VerticalPanel();

            Button searchButton = new Button();

            searchButton.setStyleName("lookingGlass");

            Anchor searchExamples = new Anchor();

            HorizontalPanel searchAndButtonpanel = new HorizontalPanel();

            searchExamples.setText(constants.searchExamplesLink());

            searchExamplePanel.setStyleName("search_examples");

            searchExamples.setWidth(Window.getClientWidth() / 6 + "px");

            searchExamplePanel.add(searchExamples);

            searchPanel.add(searchExamplePanel);

            searchExamples.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {


                    //todo call server to get examples.
                    SearchExamplesPopUp popupPanel = new SearchExamplesPopUp(searchElastic, vp, loadingProgressImage
                    );

                    popupPanel.buildLoadingMessage();

                    popupPanel.build();

                    popupPanel.show();

                }
            });

            searchPanel.setCellHorizontalAlignment(searchExamples, HorizontalPanel.ALIGN_LEFT);

            searchAndButtonpanel.setWidth("100%");

            searchAndButtonpanel.add(searchElastic);

            searchAndButtonpanel.setCellHorizontalAlignment(searchElastic, HorizontalPanel.ALIGN_LEFT);

            searchAndButtonpanel.setCellVerticalAlignment(searchElastic, HasVerticalAlignment.ALIGN_MIDDLE);

            searchAndButtonpanel.add(searchButton);

            searchAndButtonpanel.add(loadingProgressImage);

            searchPanel.add(searchAndButtonpanel);

            searchPanel.setCellWidth(searchExamples, (Window.getClientWidth() / 6) + "px");

            searchPanel.setCellWidth(searchAndButtonpanel, (Window.getClientWidth() / 6) * 5 + "px");
            searchPanel.add(numberOfloadedLetters);
            searchPanel.setStyleName("searchPanel");

            searchPanel.setCellVerticalAlignment(searchAndButtonpanel, HasVerticalAlignment.ALIGN_MIDDLE);


            titlePanel.add(searchPanel);

            titlePanel.setCellHorizontalAlignment(searchPanel, HasHorizontalAlignment.ALIGN_LEFT);

            menuIcon.setUrl("menuIconDefault.png");

           menuLink.setTargetHistoryToken("home");

            smallTitle.setVisible(true);

            titlePanel.add(menuLink);
            
            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);
            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

            searchButton.addClickHandler(searchEvent -> {

                vp.getLettersContainer().clear();

                loadingProgressImage.setVisible(true);
                TolstoyService.App.getInstance().getSelectedLetters(searchElastic.getText(), searchFacets,
                        new SearchMaterialAsynchCallback(vp,loadingProgressImage));

            });


        }
        else if ("comments".equalsIgnoreCase(value))
        {
            setComment();
            manageMenuStyles(value);

        }

    }

    private void manageMenuStyles(String value)
    {

        for (Hyperlink menu: menuItems)
        {

            if (value.equalsIgnoreCase(menu.getText()))
            {

                menu.addStyleName("navigationSel");
            }
            else
            {
                menu.removeStyleName("navigationSel");
                menu.addStyleName("navigation");
            }
        }
    }

    private void clearStyles()
    {
        smallTitle.setVisible(false);

        for (Hyperlink menu: menuItems)
        {
            menu.removeStyleName("navigationSel");
            menu.removeStyleName("navigation");
        }
    }


    private void setAbout()
    {
        flowPanel.clear();

        HTML par1 = new HTML(constants.par1());

        par1.addStyleName("textPara");

        HTML par2 = new HTML(constants.par2());

        par2.addStyleName("textPara");

        HTML par3 = new HTML(constants.par3());

        par3.addStyleName("textPara");

        HTML par4 = new HTML(constants.par4());

        par4.addStyleName("textPara");

        HTML par5 = new HTML(constants.par5());

        par5.addStyleName("textPara");

        HTML par6 = new HTML(constants.par6());

        par6.addStyleName("textPara");

        HTML par7 = new HTML(constants.par7());

        par7.addStyleName("textPara");

        HTML parSignatureName = new HTML(constants.parSignatureName());
        parSignatureName.addStyleName("textSign");
        HTML parSignatureDept = new HTML(constants.parSignatureDept());
        parSignatureDept.addStyleName("textSign");
        HTML parSignatureDeptLine2 = new HTML(constants.parSignatureDeptLine2());
        parSignatureDeptLine2.addStyleName("textSign");

        HTML parSignatureInst = new HTML(constants.parSignatureInst());
        parSignatureInst.addStyleName("textSign");
        HTML parSignatureCity = new HTML(constants.parSignatureCity());
        parSignatureCity.addStyleName("textSign");

        SafeHtmlBuilder builder = new SafeHtmlBuilder();

        builder.appendEscaped(constants.parPersonalPage());

        Anchor parSignatureLink = new Anchor(builder.toSafeHtml(), constants.parSignatureLink());

        parSignatureLink.addStyleName("textSign");


        //  VerticalPanel panel = new VerticalPanel();
        flowPanel.addStyleName("flow");


        flowPanel.add(par1);
        flowPanel.add(par2);
        flowPanel.add(par3);
        flowPanel.add(par4);
        flowPanel.add(par5);
        flowPanel.add(new HTML("&nbsp;"));
        flowPanel.add(parSignatureName);
        flowPanel.add(parSignatureDept);
        flowPanel.add(parSignatureDeptLine2);
        flowPanel.add(parSignatureInst);
        flowPanel.add(parSignatureCity);
        flowPanel.add(parSignatureLink);

    }


    private void setIndex()
    {
        consoleLog("setting index");

        mainPanel.clear();
        mainPanel.setWidget(mainContentScroll);

        mainContentScroll.clear();

        mainContentScroll.setStyleName("index_panel_style");
        
        flowPanelMain.clear();

        mainContentScroll.setWidget(flowPanelMain);

        flowPanelMain.setStyleName("flowMain");

        Image img = new Image("images/searchEx1.png");

        img.setStyleName("intro_image");

        Image img2 = new Image("images/searchEx2.png");

        img2.setStyleName("intro_image");

        HTML par7 = new HTML(constants.par7());

        par7.addStyleName("textPara");

        HTML par8 = new HTML(constants.par8());

        par8.addStyleName("textPara");

        HTML par10 = new HTML(constants.par10());

        par10.addStyleName("textPara");

        HTML parRef1 = new HTML(constants.parRef1());

        parRef1.addStyleName("textRef");


        flowPanelMain.add(par7);

        flowPanelMain.add(par8);

//        flowPanelMain.add(dateSearchAnchor);
        flowPanelMain.add(new HTML("&nbsp;"));
        flowPanelMain.add(img);

        HTML par11 = new HTML(constants.par11());

        par11.addStyleName("textPara");

        flowPanelMain.add(par11);
        flowPanelMain.add(img2);
//        flowPanelMain.add(par9);
//        flowPanelMain.add(lesMiserablesAnchor);
        flowPanelMain.add(new HTML("&nbsp;"));
        flowPanelMain.add(par10);
        flowPanelMain.add(new HTML("<hr>"));
        flowPanelMain.add(parRef1);

    }

    private void setTechnical()
    {
        mainPanel.clear();
        mainPanel.setWidget(mainContentScroll);

        mainContentScroll.setStyleName("index_panel_style");

        mainContentScroll.clear();

        mainContentScroll.setWidget(flowPanelMain);

        flowPanelMain.clear();

        HTML par1 = new HTML(constants.par1());
        par1.addStyleName("textPara");

        HTML par2 = new HTML(constants.par2());

        par2.addStyleName("textPara");

        HTML par3 = new HTML(constants.par3());

        par3.addStyleName("textPara");

        HTML par4 = new HTML(constants.par4());

        par4.addStyleName("textPara");

        HTML par5 = new HTML(constants.par5());

        par5.addStyleName("textPara");

        HTML par6 = new HTML(constants.par6());

        par6.addStyleName("textPara");

        HTML par7 = new HTML(constants.par7());

        par7.addStyleName("textPara");

        //  VerticalPanel panel = new VerticalPanel();


        // flowPanel.add(titleBar);


        flowPanelMain.add(par1);
        flowPanelMain.add(par2);
        flowPanelMain.add(par3);
        flowPanelMain.add(par4);
        flowPanelMain.add(par5);
//        flowPanel.add(dateSearchAnchor);
        flowPanelMain.add(new HTML("&nbsp;"));

        flowPanelMain.add(par6);
        flowPanelMain.add(new HTML("&nbsp;"));
        flowPanelMain.add(par7);

    }


    private void setComment()
    {
        mainPanel.clear();

        flowPanelMain.clear();

        flowPanelMain.setStyleName("flowComments");

        mainPanel.setWidget(flowPanelMain);

        VerticalPanel verticalPanel = new VerticalPanel();

        final HTML label = new HTML();

        verticalPanel.add(label);

        label.setStyleName("feedback");

        label.setHTML("&nbsp;");

        Image hand = new Image("hand.png");
        hand.setStyleName("hand");

        final TextBox emailAddress = new TextBox();
        emailAddress.setText(constants.emailLabel());

        emailAddress.addFocusHandler(new FocusHandler()
        {
            @Override
            public void onFocus(FocusEvent event)
            {
                if (constants.emailLabel().equalsIgnoreCase(emailAddress.getText()))
            {
                emailAddress.setText("");
                emailAddress.removeStyleName("commentTextDefault");
                emailAddress.addStyleName("commentText");


            }
                label.setHTML("&nbsp;");

            }
        });

        emailAddress.addBlurHandler(new BlurHandler()
        {
            @Override
            public void onBlur(BlurEvent event)
            {
                if (emailAddress.getText() !=null && emailAddress.getText().length() < 1)
                {
                    emailAddress.setText(constants.emailLabel());
                    emailAddress.removeStyleName("commentText");
                    emailAddress.addStyleName("commentTextDefault");
                }

            }
        }) ;

        emailAddress.setWidth("30em");
        emailAddress.addStyleName("commentTextDefault");


        final TextArea textArea = new TextArea();
        textArea.setWidth("30em");
        textArea.setVisibleLines(15);
        textArea.setText(constants.commentsLabel());
        textArea.addStyleName("commentText");

        textArea.addFocusHandler(new FocusHandler()
        {
            @Override
            public void onFocus(FocusEvent event)
            {
                if (constants.commentsLabel().equalsIgnoreCase(textArea.getText()))
                {
                    textArea.setText("");
                    textArea.removeStyleName("commentTextDefault");
                    textArea.addStyleName("commentText");


                }
                label.setHTML("&nbsp;");
            }


        });


        textArea.addBlurHandler(new BlurHandler()
        {
            @Override
            public void onBlur(BlurEvent event)
            {
              if (textArea.getText() !=null && textArea.getText().length() < 1)
              {
                  textArea.setText(constants.commentsLabel());
                  textArea.removeStyleName("commentText");
                  textArea.addStyleName("commentTextDefault");
              }

            }
        });


        Button submitButton = new Button(constants.submitComment());

        submitButton.addStyleName("buttonRed");

        verticalPanel.add(hand);

        HorizontalPanel emailPanel = new HorizontalPanel();
        emailPanel.setStyleName("email_panel");

        emailPanel.add(emailAddress);
        HorizontalPanel commentPanel = new HorizontalPanel();

        commentPanel.add(textArea);

        verticalPanel.add(emailPanel);
        verticalPanel.add(commentPanel);
        verticalPanel.add(submitButton);
        verticalPanel.setCellHorizontalAlignment(emailPanel, HasHorizontalAlignment.ALIGN_LEFT);
        verticalPanel.setCellHorizontalAlignment(commentPanel, HasHorizontalAlignment.ALIGN_LEFT);
        verticalPanel.setCellHorizontalAlignment(submitButton, HasHorizontalAlignment.ALIGN_LEFT);


        submitButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                if (emailAddress.getText() != null

                        && textArea.getText() != null && textArea.getText().length() > 5)
                {
                    TolstoyService.App.getInstance().submitComments(emailAddress.getText(), textArea.getText(), new CommentsAsyncCallback(label));

                } else
                {
                    label.setText(constants.emailFeedbackErr1());
                }
            }
        });

        flowPanelMain.add(verticalPanel);

    }


    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

}
