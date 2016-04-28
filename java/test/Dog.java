package test;

public class Dog implements Pet{
  String name;
  String species;
  
  protected Dog(String name, String species){
    this.name = name;
    this.species = species;
  }
  
  public String getName(){return name;}
  public String getSpecies(){return species;}  
}