package us.colloquy.tolstoy.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class LetterDisplay implements Serializable
{
    private Date date;
    private String toWhoom;
    private String content;
    private List<String> notes = new ArrayList<>();
    private String originalText;
    private List<Annotation> annotations = new ArrayList<>();
    private String source;


    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getToWhoom()
    {
        return toWhoom;
    }

    public void setToWhoom(String toWhoom)
    {
        this.toWhoom = toWhoom;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public void setNotes(List<String> notes)
    {
        this.notes = notes;
    }

    public String getOriginalText()
    {
        return originalText;
    }

    public void setOriginalText(String originalText)
    {
        this.originalText = originalText;
    }

    public List<Annotation> getAnnotations()
    {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = annotations;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }
}
