package org.ietf.jgss;

import java.util.regex.*;
import java.util.Arrays;
import java.io.*;

import edu.mit.jgss.swig.gss_OID_desc;
import edu.mit.jgss.OidUtil;

public class Oid {

    private gss_OID_desc oid;
    private byte[] derOid;
    
    /**
     * Creates a new Oid object from a string representation of the Oid's
     * integer components (e.g., "1.2.840.113554.1.2.2").
     *
     * @param strOid the string representation of the Oid, separated by dots.
     */
    public Oid(String strOid) throws GSSException {

        boolean matches = OidUtil.verifyOid(strOid);
        
        if (matches) {
            this.oid = new gss_OID_desc(strOid);
        } else {
            System.out.println("failed to verify OID string");
            throw new GSSException(GSSException.FAILURE);
        }
    }

    /**
     * Creates a new Oid object from its DER encoding. The DER encoding
     * refers to the full encoding, including the tag and length.
     * Structure and encoding of Oids is defined in ISOIEC-8824 and
     * ISOIEC-8825.
     *
     * @param derOid the stream containing the DER-encoded Oid.
     */
    public Oid(InputStream derOid) throws GSSException {

        if (derOid == null) {
            throw new NullPointerException();
        }

        try {
            byte[] tmpOid = OidUtil.OidStream2DER(derOid);
            boolean valid = OidUtil.verifyOid(tmpOid);

            if (!valid) {
                throw new GSSException(GSSException.FAILURE);
            }
        
            /* Remove tag and length from byte array */
            byte[] shortDerOid = new byte[tmpOid.length - 2];
            for (int i = 0; i < tmpOid.length-2; i++) {
                shortDerOid[i] = tmpOid[i+2];
            }
            this.oid = new gss_OID_desc(shortDerOid);

            /* store der-encoded Oid in object for future use */ 
            this.derOid = new byte[tmpOid.length];
            System.arraycopy(tmpOid, 0, this.derOid, 0, tmpOid.length);

            } catch (IOException e) {
                throw new GSSException(GSSException.FAILURE);
            }
    }

    /**
     * Creates a new Oid object from its DER encoding. The DER encoding
     * refers to the full encoding, including the tag and length.
     * Structure and encoding of Oids is defined in ISOIEC-8824 and
     * ISOIEC_8825.
     *
     * @param derOid the byte array containing the DER-encoded Oid.
     */
    public Oid(byte[] derOid) throws GSSException {

        if (derOid == null) {
            throw new NullPointerException();
        }
        
        boolean valid = OidUtil.verifyOid(derOid);
        if (!valid) {
            throw new GSSException(GSSException.FAILURE);
        }

        /* Remove tag and length from byte array */
        byte[] shortDerOid = new byte[derOid.length - 2];
        for (int i = 0; i < derOid.length-2; i++) {
            shortDerOid[i] = derOid[i+2];
        }
        this.oid = new gss_OID_desc(shortDerOid);
       
        /* store der-encoded Oid in object for future use */ 
        this.derOid = new byte[derOid.length];
        System.arraycopy(derOid, 0, this.derOid, 0, derOid.length);
    }

    /**
     * Returns a string representation of the Oid's integer components in
     * the dot separated notation.
     *
     * @return string representation of the Oid's integer components in dot
     * notation.
     */
    public String toString() {
        if (oid != null) {
            //return (this.oid).toString();

            /* Get native OID string, { X X ... X } representation */
            String tmpString = (this.oid).toString();
            String[] stringArray = tmpString.split(" ");

            /* Convert to dot-separated representation */
            StringBuilder oidString = new StringBuilder();
            for (int i = 0; i < stringArray.length; i++) {
                if (stringArray[i].matches("\\d*")) {
                    oidString.append(stringArray[i]);
                    if ((stringArray.length - i) > 2) {
                        oidString.append(".");
                    }
                }
            }
            return oidString.toString();

        } else {
            return null;
        }
    }

    /**
     * Compares two Oid objects, returning "true" if the two represent
     * the same Oid value. Two Oid objects are equal when the
     * integer result from hashCode() method called on them is the same.
     *
     * @param obj the Oid object with which to compare.
     * @return "true" if the two objects are equal, "false" otherwise.
     */
    public boolean equals(Object obj) {

        if (! (obj instanceof Oid))
            return false;

        Oid tmp = (Oid) obj;

        if (tmp == null)
            throw new NullPointerException("Input Obj is null");

        if (Arrays.equals(this.getDER(), tmp.getDER()))
            return true;
        else
            return false;
    }

    /**
     * Returns the full ASN.1 DER encoding for the Oid object. This encoding
     * includes the tag and length.
     * 
     * @return full ASN.1 DER encoding for the Oid object.
     */
    public byte[] getDER() {

        if (derOid == null) {
            byte[] tmpDerOid = OidUtil.OidString2DER(this.toString());
            return tmpDerOid;
            //System.arraycopy(tmpDerOid, 0, this.derOid, 0, tmpDerOid.length);
            //this.derOid = OidUtil.OidString2DER(this.toString());
        }

        return this.derOid;
    }

    /**
     * Tests if an Oid object is contained in the given Oid object array.
     *
     * @param oids the array of Oid objects to test against.
     * @return "true" if the Oid object is contained in the array,
     * "false" otherwise.
     */
    public boolean containedIn(Oid[] oids) {

        if (oids == null)
            throw new NullPointerException("Input Oid[] is null");

        for (int i = 0; i < oids.length; i++)
        {
            if (oid.equals(oids[i].oid))
                return true;
        }
            return false;
    }

    /**
     * package-scope method used in GSSName.java to create a new Oid
     * object.
     *
     * @param strOid Oid in dot-separated String representation
     * @return new Oid object matching input string
     */
    static Oid getNewOid(String strOid) {

        Oid retOid = null;

        try {
            retOid = new Oid(strOid);
        } catch (GSSException e) {
            e.printStackTrace();
        }

        return retOid;
    }

    public gss_OID_desc getNativeOid() {
        return  this.oid;
    }

    public void setNativeOid(gss_OID_desc newOid) {
        if (newOid != null) {
            this.oid = newOid;
            this.derOid = null;
        }
    }
}