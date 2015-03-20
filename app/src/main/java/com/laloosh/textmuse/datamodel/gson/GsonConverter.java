package com.laloosh.textmuse.datamodel.gson;

import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;

public class GsonConverter {

    public static GsonBuilder registerDateTime(GsonBuilder builder)
    {
        if (builder == null) { throw new NullPointerException("builder cannot be null"); }

        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());

        return builder;
    }


}
