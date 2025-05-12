import java.time.LocalDate;

public class CountryWithProvinces extends Country
{
    private Country[] provinces;


    public CountryWithProvinces(String name, Country[] provinces)
    {
        super(name);
        this.provinces=provinces;
    }


    // Nadpisanie metody abstrakcyjnej getConfirmedCases z klasy Country
    @Override
    public int getConfirmedCases(LocalDate date)
    {
        int total = 0; // Zmienna do zsumowania przypadków z wszystkich prowincji

        // Iterujemy przez każdą prowincję należącą do kraju
        for (Country province : provinces)
        {
            // Dodajemy liczbę przypadków z danej prowincji dla podanej daty
            total += province.getConfirmedCases(date);
        }

        // Zwracamy łączną liczbę przypadków ze wszystkich prowincji
        return total;
    }

    // Nadpisanie metody abstrakcyjnej getDeaths z klasy Country
    @Override
    public int getDeaths(LocalDate date)
    {
        int total = 0; // Zmienna do zsumowania zgonów z wszystkich prowincji

        // Iterujemy przez każdą prowincję należącą do kraju
        for (Country province : provinces)
        {
            // Dodajemy liczbę zgonów z danej prowincji dla podanej daty
            total += province.getDeaths(date);
        }

        // Zwracamy łączną liczbę zgonów ze wszystkich prowincji
        return total;
    }
}
