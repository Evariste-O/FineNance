package Entities;
import java.io.Serializable;

public class Product implements Serializable{

    private String bonName;

    private String name;

    private String price;


    public String getName() {
        return name;
    }

    public String getBonName() {
        return bonName;
    }

    public String getPrice() {
        return price;
    }

    public void setBonName(String bonName) {
        this.bonName = bonName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Product (String bonName, String name, String price){
        this.bonName = bonName;
        this.name = name;
        this.price = price;
    }
    public Product (){
        this.bonName = "";
        this.name = "";
        this.price = "";
    }

}
