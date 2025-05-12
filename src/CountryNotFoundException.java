public class CountryNotFoundException extends Exception
{
    private String countryName;

    // Konstruktor, który przyjmuje nazwę kraju
    public CountryNotFoundException(String countryName)
    {
        super("Nie znaleziono karju"+countryName);// Przekazanie komunikatu do konstruktora Exception
        this.countryName=countryName;
    }

    @Override
    // Przesłonięcie metody getMessage() by zwrócić nazwę nieznalezionego kraju
    public String getMessage()
    {
        return countryName;// Zwrócenie samej nazwy nieznalezionego kraju
    }


}
