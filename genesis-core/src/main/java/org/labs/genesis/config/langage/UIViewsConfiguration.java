package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class UIViewsConfiguration {
    private int id;
    private String name;
    private int langageId;
    private Layout layout;
    private Insert insert;
    private Update update;
    private String template;
    private String listTemplate;
    private String createTemplate;

    @Getter
    @Setter
    @ToString
    public static class Layout {
        private Menu menu;
        private String name;
        private Header header;
        private Footer footer;
        private Content content;
        private TableLoop tableLoop;
        private String destinationPath;
    }

    @Getter
    @Setter
    @ToString
    public static class Header {
        private String iconsLink;
        private String coresLink;
        private String themeLink;
        private String assetsLink;
        private String vendorsLink;
        private String helpersLink;
        private String viewAttribute;
    }

    @Getter
    @Setter
    @ToString
    public static class Menu {
        private String logo;
        private String aside;
        private String listLink;
        private String createLink;
    }

    @Getter
    @Setter
    @ToString
    public static class Content {
        private String callMenu;
        private String textFailed;
        private String callContent;
        private String alertFailed;
        private String textSuccess;
        private String alertSuccess;
    }

    @Getter
    @Setter
    @ToString
    public static class TableLoop {
        private String dataLoop;
        private String dataKeys;
        private String dataValues;
        private String dataForeignValues;

        private String dataTextOption;
        private String dataSelectLoop;
        private String dataValueOption;

        private String dataCancelId;
        private String dataModificationId;

        private String dataCancelButton;
        private String dataModificationButton;

        private String dataCancelTitleModal;
        private String dataModificationTitleModal;

        private String dataEditEventButton;
        private String dataDeleteEventButton;

        private String formCreateLink;
        private String formUpdateLink;
        private String formDeleteLink;
    }

    @Getter
    @Setter
    @ToString
    public static class Footer {
        private String coresFooterLink;
        private String pagesFooterLink;
        private String mainsFooterLink;
        private String vendorsFooterLink;
    }

    @Getter
    @Setter
    @ToString
    public static class Insert {
        private Map<String, Object> input;
        private String select;
    }

    @Getter
    @Setter
    @ToString
    public static class Update {
        private Map<String, Object> input;
        private String select;
    }

}
