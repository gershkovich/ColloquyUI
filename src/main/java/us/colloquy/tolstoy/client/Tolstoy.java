package us.colloquy.tolstoy.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.media.client.Video;
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

    public static final Hidden numberOfLoadedLetters = new Hidden();

    public static final Hidden totalNumberOfLetters = new Hidden();

    public static final Hidden loadInProgress = new Hidden();

//    private final Hyperlink descriptionLink = new Hyperlink(constants.about(), "show");

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

    String localeName = "";

    public final static TextBox searchTextBox = new TextBox();


    public void onModuleLoad()
    {

        numberOfLoadedLetters.setValue("0");

        Window.setTitle(constants.projectTitle());

        HorizontalPanel titleBar = new HorizontalPanel();

        titleBar.setStyleName("titleBar");

        titleBar.setWidth("100%");

        dockLayoutPanel.addNorth(titleBar, 6);

        localeName = LocaleInfo.getCurrentLocale().getLocaleName();

        VerticalPanel entireVerticalTitlePanel = new VerticalPanel();

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
       // titlePanel.setHeight("100%");


        titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);
        titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

        titleBar.add(entireVerticalTitlePanel);

        dockLayoutPanel.addEast(contentPanel, 40);
        dockLayoutPanel.setWidgetSize(contentPanel,0);

        //done with tile section

        setInitialCentralPanelLayout();

        //build central menu panel
        setMainCentralMenuPanel();

        mainPanel.setWidth("100%");

        mainPanel.setWidget(mainCentralVerticalPanel);

        dockLayoutPanel.add(mainPanel);


        Label enterIcon = new Label();

        enterIcon.getElement().setId("enter_icon");
        enterIcon.setStyleName("circleContainer");

        enterIcon.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                History.newItem("search");

            }
        });

        mainCentralVerticalPanel.add(enterIcon);
        

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


    //  scrollPanel.setHeight((Window.getClientHeight() - 80) + "px");

        Window.addResizeHandler(new ResizeHandler() {

            Timer resizeTimer = new Timer() {
                @Override
                public void run() {

                   // mainContentScroll.setHeight((Window.getClientHeight() - 110) + "px");

             //       scrollPanel.setHeight((Window.getClientHeight() - 80) + "px");

                  //  mainCentralVerticalPanel.setHeight((Window.getClientHeight() - 110) + "px");

                }
            };

            @Override
            public void onResize(ResizeEvent event) {
                resizeTimer.cancel();
                resizeTimer.schedule(250);
            }
        });

        RootLayoutPanel rp = RootLayoutPanel.get();

        rp.setStyleName("intro_background");

        rp.add(dockLayoutPanel);

        // Add history listener
        History.addValueChangeHandler(this);

        String initToken = History.getToken();

        if (initToken.length() == 0)
        {
            History.newItem("home");

            // Now that we've setup our listener, fire the initial history state.

        } else if (initToken.matches(".*-.*"))
        {
            History.newItem("search");
        }

        History.fireCurrentHistoryState();


        if (Document.get().getElementById("enter_icon") != null && !Document.get().getElementById("enter_icon").hasChildNodes())
        {
            generateIcons("#enter_icon", constants.search(), "Some help text");
        }


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


//        navSearch.setText(constants.search());
//        navSearch.setTargetHistoryToken("search");

//        navTechnical.setText(constants.technicalOverview());
//        navTechnical.setTargetHistoryToken("technology");

        navComment.setText(constants.comments());
        navComment.setTargetHistoryToken("comments");

//        descriptionLink.setText(constants.about());
//        descriptionLink.setTargetHistoryToken("show");

        menuItems.add(navHome);
        menuItems.add(navIndex);
//        menuItems.add(navSearch);
////        menuItems.add(navTechnical);
        menuItems.add(navComment);
//        menuItems.add(descriptionLink);



        // mainCentralVerticalPanel.setHeight("100%");

//        mainContentScroll.setWidth("100%");
//        mainContentScroll.setHeight("100%");

        HorizontalPanel menuContainer = new HorizontalPanel();

        menuContainer.setWidth("40%");

        menuContainer.addStyleName("layoutStyleMenu");

        menuContainer.add(navHome);
        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navIndex);
        menuContainer.add(new HTML("&nbsp;"));
//        menuContainer.add(navSearch);
//        menuContainer.add(new HTML("&nbsp;"));
//        menuContainer.add(navTechnical);
//        menuContainer.add(new HTML("&nbsp;"));
        menuContainer.add(navComment);
        menuContainer.add(new HTML("&nbsp;"));
//        menuContainer.add(descriptionLink);
//        menuContainer.add(new HTML("&nbsp;"));

        menuContainer.setCellHorizontalAlignment(navHome, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navIndex, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navSearch, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navTechnical, HasHorizontalAlignment.ALIGN_LEFT);
        menuContainer.setCellHorizontalAlignment(navComment, HasHorizontalAlignment.ALIGN_LEFT);
//        menuContainer.setCellHorizontalAlignment(descriptionLink, HasHorizontalAlignment.ALIGN_LEFT);

        titlePanel.add(menuContainer);

        titlePanel.setCellVerticalAlignment(menuContainer, HasVerticalAlignment.ALIGN_MIDDLE);


        mainContentScroll.setWidget(flowPanelMain);
        mainContentScroll.setAlwaysShowScrollBars(true);


       // mainContentScroll.setHeight((Window.getClientHeight() - 110) + "px");

        manageMenuStyles("home");

    }

    private void setInitialCentralPanelLayout()
    {
        Label projectSubTitle = new Label(constants.projectTitle2());

        projectSubTitle.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                    History.newItem("search");
              

            }
        });

        projectSubTitle.setStyleName("subtitle");

        mainCentralVerticalPanel.addStyleName("layoutStyle");

        mainCentralVerticalPanel.add(projectSubTitle);
        
        mainCentralVerticalPanel.add(new HTML());

        mainCentralVerticalPanel.setCellHorizontalAlignment(projectSubTitle, HasHorizontalAlignment.ALIGN_CENTER);

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
        
        consoleLog("event: " + value);

      //  dockLayoutPanel.setWidgetHidden(contentPanel, true);  //hide first

        if ("show".equalsIgnoreCase(value))
        {
            History.newItem("home");

            dockLayoutPanel.setWidgetSize(contentPanel,40);
            //dockLayoutPanel.setWidgetHidden(contentPanel, false);

           // descriptionLink.setTargetHistoryToken("hide");


        } else if ("hide".equalsIgnoreCase(value))
        {

           // dockLayoutPanel.setWidgetHidden(contentPanel, true);
            dockLayoutPanel.setWidgetSize(contentPanel,0);
            //descriptionLink.setTargetHistoryToken("show");


//        navAboutPanel.setStyleName(css.navigationItemPanelSelected());

//        setAbout(css);

        } else if ("home".equalsIgnoreCase(value))
        {
            dockLayoutPanel.setWidgetSize(contentPanel,0);
           // descriptionLink.setTargetHistoryToken("show");

            clearStyles();

            titlePanel.clear();

            addAppTitle();

            setMainCentralMenuPanel();

            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

         //   menuLink.setTargetHistoryToken("menuItems");

            mainPanel.setWidget(mainCentralVerticalPanel);

           // dockLayoutPanel.setWidgetHidden(contentPanel, true);
            dockLayoutPanel.setWidgetSize(contentPanel,0);

            menuIcon.setUrl("menuIconActive.png");

            if ( !Document.get().getElementById("enter_icon").hasChildNodes())
            {
                generateIcons("#enter_icon", constants.search(), "Some help text");
            }

            RootLayoutPanel.get().setStyleName("initial_background");

        } else if ("menuItems".equalsIgnoreCase(value))
        {
            titlePanel.clear();

            clearStyles();

            addAppTitle();
            dockLayoutPanel.setWidgetSize(contentPanel,0);
           // dockLayoutPanel.setWidgetHidden(contentPanel, true);


            mainPanel.setWidget(mainContentScroll);
            menuLink.setTargetHistoryToken("home");


            manageMenuStyles("introduction");

            setIntroduction();

           // titlePanel.add(menuLink);  //todo remove menu link
            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);

            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

            RootLayoutPanel.get().setStyleName("initial_background");

        } else if ("technology".equalsIgnoreCase(value) )
        {
            setTechnical();
            manageMenuStyles(value);

        } else if ("introduction".equalsIgnoreCase(value))
        {
            //set content
            dockLayoutPanel.setWidgetSize(contentPanel,0);
          //  descriptionLink.setTargetHistoryToken("show");
          
            setIntroduction();
            manageMenuStyles(value);

            RootLayoutPanel.get().setStyleName("intro_background");


        }  else if ("search".equalsIgnoreCase(value) )
        {
            clearStyles();
            //menuLink.setTargetHistoryToken("search");
            mainPanel.clear();
            mainPanel.setStyleName("comment_main");
          //  visualizationPanel.setSize("100%","100%");
           // visualizationPanel.add(new Label("search"));


            Hidden numberOfloadedLetters = new Hidden();
            numberOfloadedLetters.setDefaultValue("0");
            numberOfloadedLetters.setValue("0");

            //this is default
            searchFacets.getIndexesList().add("tolstoy_letters");
            searchFacets.getIndexesList().add("tolstoy_diaries");

            final  VisualisationPanel vp = new VisualisationPanel(searchTextBox, loadingProgressImage, searchFacets, localeName );

           //make async call to get initial data for visualization

            TolstoyService.App.getInstance().getDataForCharts( searchFacets, new LoadVisualisationChart(vp));

            mainPanel.add(vp);

            searchTextBox.setStyleName("seachBox");

            searchTextBox.addKeyUpHandler(new KeyUpHandler()
            {
                @Override
                public void onKeyUp(KeyUpEvent event)
                {
                    if ( event.getNativeKeyCode() == KeyCodes.KEY_ENTER )
                    {
                        vp.getLettersContainer().clear();

                        loadingProgressImage.setVisible(true);

                        TolstoyService.App.getInstance().getSelectedLetters(searchTextBox.getText(), searchFacets,
                                new SearchMaterialAsynchCallback(vp, loadingProgressImage));
                    }

                }
            });

            titlePanel.clear();

            dockLayoutPanel.setWidgetSize(contentPanel,0);

            HorizontalPanel searchPanel = new HorizontalPanel();

            VerticalPanel searchExamplePanel = new VerticalPanel();

            Button searchButton = new Button();

            searchButton.setStyleName("lookingGlass");

            Anchor searchExamples = new Anchor();

            searchExamples.setText(constants.searchExamplesLink());

            searchExamplePanel.setStyleName("search_examples");

            searchExamples.setWidth(Window.getClientWidth() / 6 + "px");

            searchExamplePanel.add(searchExamples);

            searchPanel.add(searchExamplePanel);

            HorizontalPanel dockHorizontalPanel = new HorizontalPanel();

            searchExamplePanel.setCellVerticalAlignment(searchExamples, HasVerticalAlignment.ALIGN_TOP);


            Label facetsTabLabel =  new Label(constants.fasets());

            facetsTabLabel.setStyleName("options_tab");

            dockHorizontalPanel.add(facetsTabLabel);

           Label worksTabLabel =  new Label(constants.works());

           worksTabLabel.setStyleName("options_tab");

            dockHorizontalPanel.add(worksTabLabel);

            searchExamplePanel.add(dockHorizontalPanel);

            searchExamplePanel.setCellHorizontalAlignment(dockHorizontalPanel, HasHorizontalAlignment.ALIGN_LEFT);

            searchExamplePanel.setCellHorizontalAlignment(worksTabLabel, HasHorizontalAlignment.ALIGN_LEFT);

            facetsTabLabel.addStyleName("options_tab_sel");
            worksTabLabel.removeStyleName("options_tab_sel");

            vp.getDeckPanelForOptions().showWidget(0);


            //handle Deck panel on VisualisationPanel
             worksTabLabel.addClickHandler(new ClickHandler()
             {
                 @Override
                 public void onClick(ClickEvent event)
                 {

                     facetsTabLabel.removeStyleName("options_tab_sel");
                     worksTabLabel.addStyleName("options_tab_sel");
                     vp.getDeckPanelForOptions().showWidget(1);

                 }
             });

            facetsTabLabel.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {

                    facetsTabLabel.addStyleName("options_tab_sel");
                    worksTabLabel.removeStyleName("options_tab_sel");
                    vp.getDeckPanelForOptions().showWidget(0);

                }
            });


            searchExamples.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {

                    //todo call server to get examples.
                    SearchExamplesPopUp popupPanel = new SearchExamplesPopUp(searchTextBox, vp, loadingProgressImage
                    );


                    popupPanel.buildLoadingMessage();

                    popupPanel.build();

                    popupPanel.show();

                }
            });

            searchPanel.setCellHorizontalAlignment(searchExamples, HorizontalPanel.ALIGN_LEFT);

            searchPanel.setCellVerticalAlignment(searchExamples, HasVerticalAlignment.ALIGN_TOP);

            HorizontalPanel searchAndButtonpanel = new HorizontalPanel();

            searchAndButtonpanel.setWidth("100%");

            searchAndButtonpanel.add(searchTextBox);

            searchAndButtonpanel.setCellHorizontalAlignment(searchTextBox, HorizontalPanel.ALIGN_LEFT);

            searchAndButtonpanel.setCellVerticalAlignment(searchTextBox, HasVerticalAlignment.ALIGN_MIDDLE);

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

            menuIcon.setUrl("images/back.png");

           menuLink.setTargetHistoryToken("home");

            smallTitle.setVisible(true);

            titlePanel.add(menuLink);
            
            titlePanel.setCellHorizontalAlignment(menuLink, HasHorizontalAlignment.ALIGN_RIGHT);
            titlePanel.setCellVerticalAlignment(menuLink, HasVerticalAlignment.ALIGN_MIDDLE);

            searchButton.addClickHandler(searchEvent -> {

                vp.getLettersContainer().clear();

                loadingProgressImage.setVisible(true);
                TolstoyService.App.getInstance().getSelectedLetters(searchTextBox.getText(), searchFacets,
                        new SearchMaterialAsynchCallback(vp,loadingProgressImage));

            });

            RootLayoutPanel.get().setStyleName("search_background");

        }
        else if ("comments".equalsIgnoreCase(value))
        {
            dockLayoutPanel.setWidgetSize(contentPanel,0);
          //  descriptionLink.setTargetHistoryToken("show");

            setComment();
            manageMenuStyles(value);

            RootLayoutPanel.get().setStyleName("comment_background");

        }



    }

    private void manageMenuStyles(String value)
    {

        consoleLog("Manage Menu: " + value);


        for (Hyperlink menu: menuItems)
        {
            consoleLog("History token for each item: " + menu.getTargetHistoryToken());

            if (value.equalsIgnoreCase(menu.getTargetHistoryToken()))
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



    private void setIntroduction()
    {
        mainPanel.clear();

        VerticalPanel commentBasePanel = new VerticalPanel(); //so we can align everything correctly

        commentBasePanel.setStyleName("content_base");
        commentBasePanel.addStyleName("index_panel_style");

        flowPanelMain.clear();

        flowPanelMain.setStyleName("flowMain");

        mainPanel.setWidget(commentBasePanel);

        commentBasePanel.add(flowPanelMain);

        commentBasePanel.setCellHorizontalAlignment(flowPanelMain, HasHorizontalAlignment.ALIGN_CENTER);

        ScrollPanel commentPanel = new ScrollPanel();

        commentPanel.setWidth("100%");

        flowPanelMain.add(commentPanel);


        Label enterIcon = new Label();

        enterIcon.getElement().setId("enter_icon_intro");
        enterIcon.setStyleName("circleContainer");

        enterIcon.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                History.newItem("search");

            }
        });

        commentBasePanel.add(enterIcon);

        VerticalPanel vp = new VerticalPanel();

        commentPanel.setWidget(vp);

        Image img = new Image("images/searchEx1.png");

        img.setStyleName("intro_image");

        Image img2 = new Image("images/searchEx2.png");

        img2.setStyleName("intro_image");

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

        HTML titleMission = new HTML(constants.titleMission());

        titleMission.addStyleName("textTitle");

        HTML titleUsingPlatform = new HTML(constants.titleUsingPlatform());

        titleUsingPlatform.addStyleName("textTitle");

        HTML titleSampleSearch = new HTML(constants.titleSampleSearch());

        titleSampleSearch.addStyleName("textTitle");


        HTML parSampleSearch = new HTML(constants.parSampleSearch());

        parSampleSearch.addStyleName("textPara");


        HTML captionIntro = new HTML(constants.captionIntro());

        captionIntro.addStyleName("textTitle");

        HTML titleOnSources = new HTML(constants.titleOnSources());

        titleOnSources.addStyleName("textTitle");

        HTML titleTimeline = new HTML(constants.titleTimeline());

        titleTimeline.addStyleName("textTitle");


        HTML titleTech = new HTML(constants.titleTech());

        titleTech.addStyleName("textTitle");

        HTML titleTeam = new HTML(constants.titleTeam());

        titleTeam.addStyleName("textTitle");

        HTML titleAck = new HTML(constants.titleAck());

        titleAck.addStyleName("textTitle");





        HTML par7 = new HTML(constants.par7());

        par7.addStyleName("textPara");

        HTML par8 = new HTML(constants.par8());

        par8.addStyleName("textPara");

        HTML par10 = new HTML(constants.par10());

        par10.addStyleName("textPara");

        HTML parTatyana = new HTML(constants.parTatyana());

        parTatyana.addStyleName("textPara");

        HTML parPeter = new HTML(constants.parPeter());

        parPeter.addStyleName("textPara");

        HTML parRichie = new HTML(constants.parRichie());

        parRichie.addStyleName("textPara");

        HTML parAck = new HTML(constants.parAck());

        parAck.addStyleName("textPara");

        HTML parRef1 = new HTML(constants.parRef1());

        parRef1.addStyleName("textRef");

        vp.add(titleMission);

        vp.add(par1);

        vp.add(par2);

        vp.add(par3);

        vp.add(par4);

        vp.add(titleUsingPlatform);

        vp.add(par5);

        Video video = Video.createIfSupported();
        if (video == null) {
            RootPanel.get().add(new Label("Your browser doesn't support HTML5 Video"));
            return;
        }

        video.addSource("images/introduction.mp4", VideoElement.TYPE_MP4);
        video.setControls(true);
        video.setWidth("80%");

        vp.add(captionIntro);
        vp.add(video);
        vp.add(new HTML("&nbsp;"));

        vp.setCellHorizontalAlignment(video, HasHorizontalAlignment.ALIGN_CENTER);

        vp.add(titleSampleSearch);

        vp.add(parSampleSearch);


//        flowPanelMain.add(dateSearchAnchor);
        vp.add(new HTML("&nbsp;"));
        vp.add(img);

        HTML par11 = new HTML(constants.par11());

        par11.addStyleName("textPara");

        vp.add(par11);
        vp.add(new HTML("&nbsp;"));
        vp.add(img2);
//        flowPanelMain.add(par9);
//        flowPanelMain.add(lesMiserablesAnchor);
        vp.add(new HTML("&nbsp;"));


        vp.add(titleOnSources);

        vp.add(par6);


        vp.add(titleTimeline);
        vp.add(par7);


        vp.add(titleTech);
        vp.add(par8);

        vp.add(titleTeam);
        vp.add(parTatyana);

        vp.add(parPeter);

        vp.add(parRichie);

        vp.add(titleAck);
        vp.add(parAck);
        
       // vp.add(par10);
        vp.add(new HTML("<hr>"));

        vp.add(new HTML("&nbsp;"));
        vp.add(new HTML("&nbsp;"));
      //  vp.add(parRef1);

        vp.setHeight("100%");

        if ( !Document.get().getElementById("enter_icon_intro").hasChildNodes())
        {
            generateIcons("#enter_icon_intro", constants.search(), "Some help text");
        }

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

        mainPanel.setStyleName("comment_main");


        VerticalPanel commentBasePanel = new VerticalPanel(); //so we can align everything correctly

        commentBasePanel.setStyleName("content_base");
       // commentBasePanel.addStyleName("comment_panel_style");

        flowPanelMain.clear();

        flowPanelMain.setStyleName("flowComments");  //50em max

        mainPanel.setWidget(commentBasePanel);

        commentBasePanel.add(flowPanelMain);

        commentBasePanel.setCellHorizontalAlignment(flowPanelMain, HasHorizontalAlignment.ALIGN_RIGHT);
        

//        final HTML label = new HTML();

        Image hand = new Image("hand.png");

        hand.setStyleName("hand");

     //   flowPanelMain.add(hand);


//        flowPanelMain.add(label);
//
//        label.setStyleName("feedback");
//
//        label.setHTML("&nbsp;");

     //   flowPanelMain.setHeight(Window.getClientHeight() + "px");

//        final TextBox emailAddress = new TextBox();
//
//        emailAddress.setText(constants.emailLabel());
//
//        emailAddress.addFocusHandler(new FocusHandler()
//        {
//            @Override
//            public void onFocus(FocusEvent event)
//            {
//                if (constants.emailLabel().equalsIgnoreCase(emailAddress.getText()))
//            {
//                emailAddress.setText("");
//                emailAddress.removeStyleName("commentTextDefault");
//                emailAddress.addStyleName("commentText");
//
//
//            }
//                label.setHTML("&nbsp;");
//
//            }
//        });

//        emailAddress.addBlurHandler(new BlurHandler()
//        {
//            @Override
//            public void onBlur(BlurEvent event)
//            {
//                if (emailAddress.getText() !=null && emailAddress.getText().length() < 1)
//                {
//                    emailAddress.setText(constants.emailLabel());
//                    emailAddress.removeStyleName("commentText");
//                    emailAddress.addStyleName("commentTextDefault");
//                }
//
//            }
//        });
//
//        emailAddress.setWidth("30em");
//        emailAddress.addStyleName("commentTextDefault");


//        final TextArea textArea = new TextArea();
//        textArea.setWidth("30em");
//        textArea.setVisibleLines(10);
//        textArea.setText(constants.commentsLabel());
//        textArea.addStyleName("commentText");

//        textArea.addFocusHandler(new FocusHandler()
//        {
//            @Override
//            public void onFocus(FocusEvent event)
//            {
//                if (constants.commentsLabel().equalsIgnoreCase(textArea.getText()))
//                {
//                    textArea.setText("");
//                    textArea.removeStyleName("commentTextDefault");
//                    textArea.addStyleName("commentText");
//
//
//                }
//                label.setHTML("&nbsp;");
//            }
//
//
//        });


//        textArea.addBlurHandler(new BlurHandler()
//        {
//            @Override
//            public void onBlur(BlurEvent event)
//            {
//              if (textArea.getText() !=null && textArea.getText().length() < 1)
//              {
//                  textArea.setText(constants.commentsLabel());
//                  textArea.removeStyleName("commentText");
//                  textArea.addStyleName("commentTextDefault");
//              }
//
//            }
//        });


//        Button submitButton = new Button(constants.submitComment());

//        submitButton.addStyleName("buttonRed");

//        HorizontalPanel emailPanel = new HorizontalPanel();

//        emailPanel.add(emailAddress);
        ScrollPanel commentPanel = new ScrollPanel();

//        commentPanel.add(textArea);

//        flowPanelMain.add(emailPanel);

//        flowPanelMain.add(submitButton);
//        flowPanelMain.add(new HTML("&nbsp;"));

        Label discussLbl =new Label();
        discussLbl.getElement().setId("disqus_thread");
        commentPanel.add(discussLbl);

       // commentPanel.setHeight(Window.getClientHeight() + "px");
        commentPanel.setWidth("100%");
        discussLbl.setStyleName("disqus_comment");
        
        commentBasePanel.setCellHorizontalAlignment(discussLbl, HasHorizontalAlignment.ALIGN_CENTER);

        flowPanelMain.add(commentPanel);



      //  flowPanelMain.setHeight(Window.getClientHeight() + "px");

//        submitButton.addClickHandler(new ClickHandler()
//        {
//            public void onClick(ClickEvent event)
//            {
//                if (emailAddress.getText() != null
//
//                        && textArea.getText() != null && textArea.getText().length() > 5)
//                {
//                    TolstoyService.App.getInstance().submitComments(emailAddress.getText(), textArea.getText(), new CommentsAsyncCallback(label));
//
//                } else
//                {
//                    label.setText(constants.emailFeedbackErr1());
//                }
//            }
//        });

        addDiscuss();
    }


    native void consoleLog(String message) /*-{
        console.log("me:" + message);
    }-*/;

    native void generateIcons(String divId, String labelText, String helpText) /*-{

        $wnd.generateSiteIcons(divId, labelText, helpText);
    }-*/;

    native void addDiscuss() /*-{
        $wnd.addDiscussBlock();
    }-*/;

}
