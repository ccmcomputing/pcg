package com.philomathery.pcg.model;

import java.io.OutputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

public final class XmlUtil {
    private XmlUtil() {}

    public static void writeCertificate(Certificate cert, OutputStream out) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Certificate.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(cert, out);
    }
}


