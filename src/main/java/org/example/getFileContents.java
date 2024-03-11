package org.example;

import java.io.InputStream;
import java.util.Scanner;

public class getFileContents {
    
    public String getFileContent(String fileName){
                //Creating instance to avoid static member methods
                getFileContents instance 
                = new getFileContents();
    
            InputStream is = instance.getFileAsIOStream(fileName);
            try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
                String result = s.hasNext() ? s.next() : "";
                return result;
            }
    }

        private InputStream getFileAsIOStream(final String fileName) 
    {
        InputStream ioStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(fileName);
        
        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }
}
