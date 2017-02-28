import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by Trym Todalshaug on 27/02/2017.
 */
public class KontoDAO {
    private EntityManagerFactory emf;

    /* OBS! EntityManagerFactory er thread safe, men det er ikke
    * EntityManger! Objektvariabel medfører at vi må synkronisere metodene.
    * Vi løser det med å ha EntityManger bare lokalt. Unngår trådproblematikk!
    */
    //private EntityManager em;

    public KontoDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    //Metoden er lagret for ålagre nye bøker
    //Merk at persist() vile fungere som SQL INSERT
    //Kontoen (kontonr) kan derfor ikke være lagret i DB fra før!

    private EntityManager getEM(){
        return emf.createEntityManager();
    }

    private void lukkEM(EntityManager em){
        if (em != null && em.isOpen()) em.close();
    }

    public void lagreNyKonto(Konto konto) {
        EntityManager em = getEM();
        try {
            em.getTransaction().begin();
            em.persist(konto); ;//fører boka inn i lagringskontekt (persistence context)
            em.getTransaction().commit(); //lagring skjer her
        }finally {
            lukkEM(em);
        }
    }

    //Finner en konto basert på primærtnøkkel
    public Konto finnKonto(String kontonr) {
        EntityManager em = getEM();
        try {
            return em.find(Konto.class, kontonr);
        }finally {
            lukkEM(em);
        }
    }

    //Endrer en eksisterenede konto, vi bruker merge for å sikre at kontoen
    //føres inn i lagringskonteksten (må det om den har vært serialisert)
    public void endreKonto(Konto konto) {
        EntityManager em = getEM();
        try {
            em.getTransaction().begin();
            Konto k = em.merge(konto); //sørger for å føre entiteten inn i lagringskonteksten
            em.getTransaction().commit(); //merk at endringene gjort utenfor transaksjonen blir lagret!!!
        } finally {
            lukkEM(em);
        }
    }

    public void slettKonto(String kontonr) {
        EntityManager em = getEM();
        try {
            Konto k = finnKonto(kontonr);
            em.getTransaction().begin();
            em.remove(k); //Remove må kalles i en transaksjon
            em.getTransaction().commit();
        } finally {
            lukkEM(em);
        }
    }

    //Spørring som henter alle bøker
    public List<Konto> getAlleKonti() {
        EntityManager em = getEM();
        try {
            Query q = em.createQuery("SELECT OBJECT(o) FROM Konto o");
            //SELECT o FROM KONTO o gir samme resultat
            //MERK at Konto må ha stor K (Eksakt samme som klassenavn)
            return q.getResultList();
        }finally {
            lukkEM(em);
        }
    }

    //Her bruker vi navngitt spørring (NamedQuery). Denne finner du i Konto-klassen
    //Slike legges altså i entitetsklassen og gir
    //mulighet for optimalisering av spørring ala PreparedStatement
    public int getAntallKonti() {
        EntityManager em = getEM();
        try {
            Query q = em.createNamedQuery("finnAntallKonti");
            Long ant = (Long)q.getSingleResult();
            return ant.intValue();
        } finally {
            lukkEM(em);
        }
    }

    //Merk at begge spørringene i metoden fungerer (en utkommentert)
    //Ofte kan nok den første være å foretrekke
    public List<Konto> getKontiForNavn(String navn) {
        EntityManager em = getEM();
        try {
            Query q = em.createQuery("SELECT OBJECT(a) FROM Konto a WHERE a.navn= :navn");
            //Query q = em.createQuery("SELECT OBJECT(a) FROM Konto a WHERE a.navn='" +navn + "'");
            q.setParameter("navn", navn);
            return q.getResultList();
        } finally {
            lukkEM(em);
        }
    }

    //Testklient
    public static void main(String args[]) throws Exception {
        EntityManagerFactory emf = null;
        KontoDAO fasade = null;
        System.out.println("Starting 2...");
        try {
            emf = Persistence.createEntityManagerFactory("kontoEntity");
            System.out.println("Constructor done " + emf);
            fasade = new KontoDAO(emf);
            System.out.println("Constructore done");

            //Lager en konto med setMetodene i Konto
            Konto konto = new Konto();
            konto.setKontonr("12345012345");
            konto.setNavn("Nils Olav Johnsen");
            konto.setSaldo(2049.36);
            fasade.lagreNyKonto(konto); //Lagrer kontoen

            //Lager ny bok med konstruktør i stedet for setMetodene
            Konto konto1 = new Konto("09876543210", "Jon Olav Nilsen", 9000.99);
            fasade.lagreNyKonto(konto1);

            Konto konto2 = new Konto("019283746574", "Jon Olav Nilsen", 8000.00);
            fasade.lagreNyKonto(konto2);

            //Skriv ut kontoene somer lagret
            System.out.println("Theses accounts have been stored in the database:");
            List<Konto> list = fasade.getAlleKonti();
            for (Konto k : list) {
                System.out.println("----" + k);
            }

            konto = (Konto)list.get(0);
            konto.setNavn("ChangedName");
            fasade.endreKonto(konto);

            konto = fasade.finnKonto(konto.getKontonr()); //Henter ut kontoen på nytt
            System.out.println("Account has now been updated. This is how it looks now: " + konto);

            //Finner antall konti i DB
            int ant = fasade.getAntallKonti();
            System.out.println("Amount of accounts in the database = " + ant);

            //Lister ut alle konti for en bestemt person
            list = fasade.getKontiForNavn("Jon Olav Nilsen");
            System.out.println("The following accounts belong to this person: " + list.size());
            for (Konto k : list) {
                System.out.println("\t" + k.getKontonr() + k.getSaldo());
            }
        } finally {
            emf.close();
        }
    }
}
