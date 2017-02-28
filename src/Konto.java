import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * Created by Trym Todalshaug on 27/02/2017.
 */
@Entity @NamedQuery(name="finnAntallKonti", query="SELECT COUNT(o) FROM Konto o")
public class Konto {
    @Id
    private String kontonr;
    private String navn;
    private double saldo;

    public Konto(){
        //Empty constructor with empty parameter list
        //JavaBeans standard
    }

    public Konto(String kontonr, String navn, double saldo){
        this.kontonr = kontonr;
        this.navn = navn;
        this.saldo = saldo;
    }

    public String getKontonr() {
        return kontonr;
    }

    public void setKontonr(String kontonr) {
        this.kontonr = kontonr;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String toString() {
        return "Kontonummer: " + kontonr +
                "\nNavn: " + navn +
                "\nSaldo: " + saldo;
    }
}
