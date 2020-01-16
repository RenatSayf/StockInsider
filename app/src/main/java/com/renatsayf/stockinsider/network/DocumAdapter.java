package com.renatsayf.stockinsider.network;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class DocumAdapter implements Converter<ResponseBody, Document>
{
    public static final Converter.Factory FACTORY = new Converter.Factory(){
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (type == Document.class) return new DocumAdapter();

            return super.responseBodyConverter(type, annotations, retrofit);
        }
    };

    @Override
    public Document convert(ResponseBody value) throws IOException {
        return Jsoup.parse(value.toString());
    }
}
