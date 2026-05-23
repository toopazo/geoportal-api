package cl.geoportal.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "division_politica_administrativa_2023")
public class DivisionPoliticaAdministrativa {

    @Id
    @Column(name = "cut_com")
    private String cutCom;

    @Column(name = "cut_reg")
    private String cutReg;

    @Column(name = "cut_prov")
    private String cutProv;

    @Column(name = "region")
    private String region;

    @Column(name = "provincia")
    private String provincia;

    @Column(name = "comuna")
    private String comuna;

    @Column(name = "superficie")
    private Double superficie;

    public String getCutCom()    { return cutCom; }
    public String getCutReg()    { return cutReg; }
    public String getCutProv()   { return cutProv; }
    public String getRegion()    { return region; }
    public String getProvincia() { return provincia; }
    public String getComuna()    { return comuna; }
    public Double getSuperficie(){ return superficie; }
}
