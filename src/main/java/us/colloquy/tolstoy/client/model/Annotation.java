package us.colloquy.tolstoy.client.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class Annotation implements Serializable
{
    private Date createdDate;
    private Date updatedDae;
    private String title;
    private String text;
    private String name; //todo perhaps a user who made an annotation

    public Date getUpdatedDae()
    {
        return updatedDae;
    }

    public void setUpdatedDae(Date updatedDae)
    {
        this.updatedDae = updatedDae;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
