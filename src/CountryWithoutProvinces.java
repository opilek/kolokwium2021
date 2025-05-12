import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Klasa reprezentująca kraj bez podziału na prowincje, dziedziczy po klasie Country
public class CountryWithoutProvinces extends Country
{

    // Wewnętrzna (statyczna) klasa reprezentująca dane z jednego dnia
    public static class dailyStats
    {
        public final LocalDate dates; // data dzienna
        public final int cases;       // liczba przypadków zachorowań tego dnia
        public final int deaths;      // liczba zgonów tego dnia

        // Konstruktor klasy dailyStats
        public dailyStats(LocalDate dates, int cases, int deaths)
        {
            this.dates = dates;
            this.cases = cases;
            this.deaths = deaths;
        }
    }


    // Lista przechowująca wszystkie dzienne wpisy statystyk
    private final List<dailyStats> dailyStats = new ArrayList<>();

    // Getter pozwalający odczytać całą listę statystyk
    public List<dailyStats> getDailyStats() {return dailyStats;}


    // Konstruktor klasy CountryWithoutProvinces — przekazuje nazwę kraju do klasy nadrzędnej
    public CountryWithoutProvinces(String name)
    {
        super(name);
    }


    // Metoda dodająca nowy dzienny wpis: datę, liczbę zachorowań i liczbę zgonów
    public void addDailyStatistic(LocalDate dates, int cases, int deaths)
    {
        dailyStats.add(new dailyStats(dates, cases, deaths));
    }

    // Nadpisanie metody abstrakcyjnej z klasy Country
    @Override
    public int getConfirmedCases(LocalDate date)
    {
        // Przechodzimy przez wszystkie zapisane dzienne statystyki
        for(dailyStats stats : dailyStats)
        {
            // Sprawdzamy, czy data w danym wpisie zgadza się z tą, o którą pytamy
            if(stats.dates.equals(date))
            {
                // Jeśli tak, zwracamy liczbę przypadków dla tej daty
                return stats.cases;
            }
        }

        // Jeśli nie znaleziono wpisu dla tej daty, zwracamy 0
        return 0;
    }

    // Nadpisanie metody abstrakcyjnej z klasy Country
    @Override
    public int getDeaths(LocalDate date)
    {
        // Przechodzimy przez wszystkie zapisane dzienne statystyki
        for(dailyStats stats : dailyStats)
        {
            // Sprawdzamy, czy data w danym wpisie zgadza się z tą, o którą pytamy
            if(stats.dates.equals(date))
            {
                // Jeśli tak, zwracamy liczbę zgonów dla tej daty
                return stats.deaths;
            }
        }

        // Jeśli nie znaleziono wpisu dla tej daty, zwracamy 0
        return 0;
    }
}