package org.example.model;

/*
EXCEL LISTING:



SQL LISTINGS
-------------
MULTI-STATE:
Aetna Behavioral Health -> AETNA
Blue Cross Blue Shield of -> BCBS
Champa VA -> CHAMPVA
Evernorth -> EVERN
Magellan Behavioral Healt -> MAGELL
UnitedHealthcare Communit -> UHCMED
United Behavioral Health -> OPTUM
UMR -> OPTUM



NC:
Alliance Health -> ALLIANCE
AmeriHealth Caritas -> AMERIH
Blue Cross Blue Shield of -> BCBS
Blue Cross NC | Healthy B -> HBLUM
Carolina Complete Health -> COMPHEA
MedCost -> MEDCOST
NC Medicaid-Partners -> PARTNER
Trillium -> TRILLIUM
Vaya Health -> VAYA
Wellcare of NC -> WELLCAR

TN:
Aetna Tennessee -> AETNA
Blue Care -> BLUECA
Blue Care Tennessee -> BLUECA
Blue Cross Blue Shield FE -> BCBS
TennCare Select -> TENNCSLT
Wellpoint -> WLPNT

SC:
Absolute Total Care -> ABSOLUTEC
BCBS South Carolina -> HBLUSC
BlueCross BlueShield of S -> BCBS
HUMANA -> HUMANA
Molina Healthcare of Sout -> MOLINA
Select Health of Sout Ca -> FIRSTSH
South Carolina Medicaid -> HEALTHCONN

CA:
Anthem Blue Cross of Cali -> ANTHEM
Blue Cross Blue Shield FE -> BCBSFEP
Blue Shield of California -> PROM
California Regional Cente -> SDRC
Carelon -> CARELON
Community Health Group -> CHG
Health Net of California -> HEALTHNET
Inland Empire Health Plan -> IEHP
LA Care Health Plan -> LACARE
Molina Healthcare of Cali -> MOLINA
Regal Medical Group, Inc. -> REGAL
School District -> OXUSD
Tri Counties Regional Cen -> TCRC




 */

public enum Insurance {
    //Multi-States
    AETNA,
    BCBS,
    EVERN,
    OPTUM,
    UHCMED,
    MAGELL,
    PP,


    //NC
    ALLIAN,
    AMERIH,
    HBLUM,
    COMPHEA,
    CHAMPVA,
    MEDCOST,
    PARTNER,
    TRILLIUM,
    VAYA,
    WELLCAR,

    //tn
    BLUECA,
    TENNCSLT,
    WLPNT,

    //SC
    ABSOLUTETC,
    FIRSTSH,
    HBLUSC,
    HEALTHCONN,
    HUMANA,
    MOLINA,

    //CA
    ANTHEM,
    CARELON,
    CHG,
    HEALTHNET,
    IEHP,
    LACARE,
    SCHOOL,
    PROM,
    REGAL,
    SDRC,
    TCRC

    ;

    public static Insurance fromString(String input) {
        if (input == null || input.isEmpty()) return null;
        String normalized = input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();


        switch (normalized) {
            //All States
            case "AETNA", "AETNABEHAVIORALHEALTH", "AETNATENNESSEE":
                return Insurance.AETNA;
            case "BCBS", "BCBSTN", "BCBSFEP","BCBSIL", "BLUECROSSBLUESHIELDOF", "BLUECROSSBLUESHIELDFE", "BLUECROSSBLUESHIELDOFS":
                return Insurance.BCBS;
            case "EVERN", "EVERNORTH":
                return Insurance.EVERN;
            case "OPTUM", "UMR", "UNITEDBEHAVIORALHEALTH":
                return Insurance.OPTUM;
            case "UHCMED", "UNITEDHEALTHCARECOMMUNIT":
                return Insurance.UHCMED;
            case "PP":
                return Insurance.PP;

            //NC
            case "ALLIAN", "ALLIANCEHEALTH":
                return Insurance.ALLIAN;
            case "AMERIH", "AMERIHEALTHCARITAS":
                return Insurance.AMERIH;
            case "HBLUM", "BLUECROSSNCHEALTHYB":
                return Insurance.HBLUM;
            case "COMPHEA", "CAROLINACOMPLETEHEALTH":
                return Insurance.COMPHEA;
            case "CHAMPVA", "CHAMPAVA":
                return Insurance.CHAMPVA;
            case "PARTNER", "NCMEDICAIDPARTNERS":
                return Insurance.PARTNER;
            case "TRILLIUM":
                return Insurance.TRILLIUM;
            case "MEDCOST":
                return Insurance.MEDCOST;
            case "VAYA", "VAYAHEALTH":
                return Insurance.VAYA;
            case "WELLCAR", "WELLCAREOFNC":
                return Insurance.WELLCAR;

            //TN
            case "COVERK", "BLUECA", "BLUECARE", "BLUECARETENNESSEE":
                return Insurance.BLUECA;
            case "MAGELL", "MAGELLAN", "MGLN", "MAGELLANBEHAVIORALHEALT":
                return Insurance.MAGELL;
            case "TENNCSLT", "TENNCARESELECT":
                return Insurance.TENNCSLT;
            case "WLPNT", "WELLPOINT":
                return Insurance.WLPNT;

            //sc
            case "ABSOLUTETC", "ABSOLUTETOTALCARE":
                return Insurance.ABSOLUTETC;
            case "HBLUSC", "BCBSSOUTHCAROLINA":
                return Insurance.HBLUSC;
            case "HUMANA":
                return Insurance.HUMANA;
            case "MOLINA", "MOLINAMED", "MOLINAHEALTHCAREOFSOUT", "MOLINAHEALTHCAREOFCALI":
                return Insurance.MOLINA;
            case "FIRSTSH", "SELECTHEALTHOFSOUTCA":
                return Insurance.FIRSTSH;
            case "HEALTHCONN", "SOUTHCAROLINAMEDICAID":
                return Insurance.HEALTHCONN;

            // ca
            case "ANTHEM", "ANTHEMMED", "ANTHEMBLUECROSSOFCALI":
                return Insurance.ANTHEM;
            case "PROM", "BSCA", "BLUESHIELDOFCALIFORNIA":
                return Insurance.PROM;
            case "CARELON":
                return Insurance.CARELON;
            case "CHG", "COMMUNITYHEALTHGROUP":
                return Insurance.CHG;
            case "HEALTHNET", "MHN", "MHNMED", "HEALTHNETMED", "HEALTHNETOFCALIFORNIA":
                return Insurance.HEALTHNET;
            case "IEHP", "INLANDEMPIREHEALTHPLAN":
                return Insurance.IEHP;
            case "LACARE", "LACAREHEALTHPLAN":
                return Insurance.LACARE;
            case "SDRC", "CALIFORNIAREGIONALCENTE":
                return Insurance.SDRC;
            case "REGAL", "REGALMEDICALGROUP", "REGALMEDICALGROUPINC":
                return Insurance.REGAL;
            case "TCRC", "TRICOUNTIESREGIONALCEN":
                return Insurance.TCRC;
            case "OXUSD", "BGCHP", "VTUSD", "MUSD", "SCHOOLDISTRICT":
                return Insurance.SCHOOL;


            default: return null;
        }
    }

}
