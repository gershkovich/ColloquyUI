package us.colloquy.tolstoy.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import us.colloquy.tolstoy.client.model.LetterDisplay;
import us.colloquy.tolstoy.client.model.ServerResponse;

/**
 * Created by Peter Gershkovich on 2/13/18.
 */
public class CommonFormatter
{
    public static void formatLetterDisplay(ServerResponse result, VerticalPanel lettersContainer)
    {
        DateTimeFormat sdf = DateTimeFormat.getFormat("MMM dd yyyy");


        String letterDisplayHeaderClass = "letter_table_display_header";

        String letterDisplayClass = "letter_table_display";
        
        for (LetterDisplay ld: result.getLetters())
        {
            String id = ld.getId();

            String toWhom = "";

            if (ld.getToWhoom()!= null && ld.getToWhoom().length() > 0)
            {
                toWhom = ld.getToWhoom() + " ";
            }

            lettersContainer.add(new HTML("<div id=\"" + ld.getId() + "\" class=\"" + letterDisplayHeaderClass + "\" >" + sdf.format(ld.getDate())
                    + " " + toWhom  + "</div>"));

            lettersContainer.add(new HTML("<div class=\"" + letterDisplayClass + "\" >" + ld.getContent() + "</div>"));

            for (String note: ld.getNotes())
            {
                lettersContainer.add(new HTML("<div class=\"" + letterDisplayClass + "\" >" + note + "</div>"));

            }

            lettersContainer.add(new HTML("<p></p>"));
        }
    }

}
