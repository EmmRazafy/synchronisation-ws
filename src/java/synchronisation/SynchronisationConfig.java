/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import synchronisable.param.UsefulParam;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.ArrayListHelper;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class SynchronisationConfig {
    private static HashMap<String, String[]> CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME = null;
    public final static List<String> SYNCHRO_NAME_LEVEL_2_PSEUDO_PARAMETRE = ArrayListHelper.newArrayList("EtablissementAccesMad");
    
    public static List<List<String>> getAllSynchroName(){
        List<List<String>> liAllSynchroName = new ArrayList<List<String>>(2);
        liAllSynchroName.add(new ArrayList<>(10));
        /*liAllSynchroName.get(0).add("NameSynchro");//ALL_SYNCHRO_NAME level 0*/
        
        liAllSynchroName.get(0).add("TypeMateriel");//TypeMtrl.
        liAllSynchroName.get(0).add("EtatMateriel");//EtatMateriel.
        liAllSynchroName.get(0).add("TypeAffectation");//TypeAffectation.
        liAllSynchroName.get(0).add("OS");//TypeAffectation.
        liAllSynchroName.get(0).add("AnneeScolaire");//TypeAffectation.
        liAllSynchroName.get(0).add("Parametre");//TypeAffectation.
        liAllSynchroName.get(0).addAll(UsefulParam.PSEUDO_PARAMETRE);
        liAllSynchroName.get(0).add("Province");
        liAllSynchroName.get(0).add("TypeEtablissement");
        liAllSynchroName.get(0).add("TypeEnseignement");
        liAllSynchroName.get(0).add("NiveauEnseignement");
        
        
        liAllSynchroName.add(new ArrayList<>(1));
        liAllSynchroName.get(1).add("Region");

        liAllSynchroName.add(new ArrayList<>(2));
        liAllSynchroName.get(2).add(SYNCHRO_NAME_LEVEL_2_PSEUDO_PARAMETRE.get(0));
        liAllSynchroName.get(2).add("EtablissementScolaire");
        
        liAllSynchroName.add(new ArrayList<>(4));
        liAllSynchroName.get(3).add("MaterielSimple");//MaterielSimple 0
        liAllSynchroName.get(3).add("Ordinateur");//Ordinateur 1
        liAllSynchroName.get(3).add("Administratif");//"Administratif" 2
        liAllSynchroName.get(3).add("Convention");//"Convention" 3
        liAllSynchroName.get(3).add("MaterielConsommable");//"MaterielConsommable" 4
        
        liAllSynchroName.add(new ArrayList<>(2));
        liAllSynchroName.get(4).add("Facture");
        liAllSynchroName.get(4).add("Charge");
        
        return liAllSynchroName;
    }

    public static String[] getCentSPeriSAndCentSStoryPeriSStoryClsName(String synchroName) throws SynchroNameNotFoundException{
        if(CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME == null){
            List<List<String>> allSynchroName = getAllSynchroName();
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME= new HashMap<>(3);
            int allSynchroNameGet0Indice=0;
            /*CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.name.NameCentSynchro","synchronisable.name.NamePeriSynchro"//synchro Cls
            });*/
           
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.type_mtrl.TypeMtrlCentSynchro", "synchronisable.type_mtrl.TypeMtrlPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.etat_mtrl.EtatMtrlCentSynchro", "synchronisable.etat_mtrl.EtatMtrlPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.type_affectation.TypeAffectationCentSynchro", "synchronisable.type_affectation.TypeAffectationPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.os.OSCentSynchro", "synchronisable.os.OSPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.annee_scolaire.AnneeScolaireCentSynchro", "synchronisable.annee_scolaire.AnneeScolairePeriSynchro"//synchro Cls
            });
            
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.ParamCentSynchro", "synchronisable.param.ParamPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.prx_u_ordi.client.PrxUOrdiClientCentSynchro", "synchronisable.param.prx_u_ordi.client.PrxUOrdiClientPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.prx_u_ordi.serveur.PrxUOrdiServeurCentSynchro", "synchronisable.param.prx_u_ordi.serveur.PrxUOrdiServeurPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.red_u_ordi.client.RedUOrdiClientCentSynchro", "synchronisable.param.red_u_ordi.client.RedUOrdiClientPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.red_u_ordi.serveur.RedUOrdiServeurCentSynchro", "synchronisable.param.red_u_ordi.serveur.RedUOrdiServeurPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.param.nbr_trch_paym.NbrTrchPmtCentSynchro", "synchronisable.param.nbr_trch_paym.NbrTrchPmtPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.province.ProvinceCentSynchro", "synchronisable.province.ProvincePeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.type_etablissmt.TypeEtablissmtCentSynchro", "synchronisable.type_etablissmt.TypeEtablissmtPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.type_enseignmt.TypeEnseignmtCentSynchro", "synchronisable.type_enseignmt.TypeEnseignmtPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(0).get(allSynchroNameGet0Indice++).toLowerCase(), new String[]{
                "synchronisable.niveau_enseignmt.NiveauEnseignmtCentSynchro", "synchronisable.niveau_enseignmt.NiveauEnseignmtPeriSynchro"//synchro Cls
            });

//         ---------------------------------------------------------------------------Level 1------------------------------------------------------------
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(1).get(0).toLowerCase(), new String[]{
                "synchronisable.region.RegionCentSynchro", "synchronisable.region.RegionPeriSynchro"//synchro Cls
            });

//         ---------------------------------------------------------------------------Level 1------------------------------------------------------------
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(2).get(0).toLowerCase(), new String[]{
                "synchronisable.lieu_affectation.LieuAffectationCentSynchro", "synchronisable.lieu_affectation.LieuAffectationPeriSynchro"//synchro Cls
            });
            
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(2).get(1).toLowerCase(), new String[]{
                "synchronisable.etablissmt_scolaire.EtablissmtScolaireCentSynchro", "synchronisable.etablissmt_scolaire.EtablissmtScolairePeriSynchro"//synchro Cls
            });
            
//         ---------------------------------------------------------------------------Level 1------------------------------------------------------------
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(3).get(0).toLowerCase(), new String[]{
                "synchronisable.mtrl_simple.MtrlSimpleCentSynchro", "synchronisable.mtrl_simple.MtrlSimplePeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(3).get(1).toLowerCase(), new String[]{
                "synchronisable.mtrl_ordi.MtrlOrdiCentSynchro", "synchronisable.mtrl_ordi.MtrlOrdiPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(3).get(2).toLowerCase(), new String[]{
                "synchronisable.admnstrtf.AdmnstrtfCentSynchro", "synchronisable.admnstrtf.AdmnstrtfPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(3).get(3).toLowerCase(), new String[]{
                "synchronisable.convention.ConventionCentSynchro", "synchronisable.convention.ConventionPeriSynchro"//synchro Cls
            });
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(3).get(4).toLowerCase(), new String[]{
                "synchronisable.mtrl_cnsmble.EventCentSynchro", "synchronisable.mtrl_cnsmble.EventPeriSynchro"//synchro Cls
            });
            
//         ---------------------------------------------------------------------------Level 1------------------------------------------------------------
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(4).get(0).toLowerCase(), new String[]{
                "synchronisable.facture.FactureCentSynchro", "synchronisable.facture.FacturePeriSynchro"//synchro Cls
            }); 
            CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.put(allSynchroName.get(4).get(1).toLowerCase(), new String[]{
                "synchronisable.convention_charge.ConventionChargeCentSynchro", "synchronisable.convention_charge.ConventionChargePeriSynchro"//synchro Cls
            });
        }
        String[] centPeriClsName = CENT_SPERI_S_AND_CENT_S_STORY_PERI_S_STORY_CLS_NAME.get(synchroName.toLowerCase());
        if(centPeriClsName == null)
            throw new SynchroNameNotFoundException("Nom de synchronisation "+ synchroName+" introuvable.");
        return centPeriClsName;
    }
}
