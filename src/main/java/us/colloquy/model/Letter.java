/*
 * Copyright (c) 2016. Tatyana Gershkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.colloquy.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/14/15.
 */
public class Letter
{
    private String id;
    private List<Person> to = new ArrayList<>();
    private Date date;
    private String place;
    private String modern;
    private String content;
    private List<String> notes = new ArrayList<>();
    private String source;
    private String toWhom; // to whom it was addressed is taken from reference - parsed volume 91
    private String documentPointer;
    private String entry;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public List<Person> getTo()
    {
        return to;
    }

    public void setTo(List<Person> to)
    {
        this.to = to;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
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

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getModern()
    {
        return modern;
    }

    public void setModern(String modern)
    {
        this.modern = modern;
    }

    public String getToWhom()
    {
        return toWhom;
    }

    public void setToWhom(String toWhom)
    {
        this.toWhom = toWhom;
    }

    public String getDocumentPointer()
    {
        return documentPointer;
    }

    public void setDocumentPointer(String documentPointer)
    {
        this.documentPointer = documentPointer;
    }

    public String getEntry()
    {
        return entry;
    }

    public void setEntry(String entry)
    {
        this.entry = entry;
    }

    @Override
    public String toString()
    {
        StringBuilder people = new StringBuilder();

        for (Person person: to)
        {
          people.append(person.toString());
        }
        return id + "\t Date: " + (date !=null ? date.toString(): "no date") + "\t Place: " + place + "\t Source:" + source
                + "\nto whom: " + getToWhom()
                + "\nto: " + people.toString() + "\n Content: " + content + "\n Notes: " + notes;
    }
}
