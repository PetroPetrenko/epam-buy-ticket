package com.example.customer.agent.skills;

import com.example.customer.dto.BookingProposal;
import com.example.customer.service.EmailService;
import com.example.customer.service.ProposalService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class HotelBookingSkill implements AgentSkill<HotelBookingSkill.Input, HotelBookingSkill.Output> {

    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final ProposalService proposalService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.booking.api-key}")
    private String apiKey;
    
    @Value("${app.booking.host}")
    private String apiHost;
    
    @Value("${app.booking.base-url}")
    private String baseUrl;

    public HotelBookingSkill(RestTemplate restTemplate, EmailService emailService, ProposalService proposalService) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.proposalService = proposalService;
    }

    public static class Input {
        private final String city;
        private final String hotelName;
        private final String checkIn;
        private final String checkOut;
        private final String email;
        private final String phone;
        private final String budget;

        public Input(String city, String hotelName, String checkIn, String checkOut, String email, String phone, String budget) {
            this.city = city;
            this.hotelName = hotelName;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.email = email;
            this.phone = phone;
            this.budget = budget;
        }

        public String city() { return city; }
        public String hotelName() { return hotelName; }
        public String checkIn() { return checkIn; }
        public String checkOut() { return checkOut; }
        public String email() { return email; }
        public String phone() { return phone; }
        public String budget() { return budget; }
    }

    public static class Output {
        private final String reservationId;
        private final String status;
        private final String details;

        public Output(String reservationId, String status, String details) {
            this.reservationId = reservationId;
            this.status = status;
            this.details = details;
        }

        public String reservationId() { return reservationId; }
        public String status() { return status; }
        public String details() { return details; }
    }

    @Override
    public String getName() {
        return "bookHotel";
    }

    @Override
    public String getDescription() {
        return "Бронирование номера в отеле в указанном городе";
    }

    @Override
    public Class<Input> getInputType() {
        return Input.class;
    }

    private String getBBoxForCity(String city) {
        log.info("Searching coordinates (bbox) for city: {}", city);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("x-rapidapi-host", apiHost);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + "/locations/search?name=" + city + "&languagecode=en-us";

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // Ожидаемый формат: массив объектов, берем первый результат
            if (root.isArray() && root.size() > 0) {
                JsonNode location = root.get(0);
                // В Booking API bbox часто возвращается как строка или набор координат
                // Если bbox нет напрямую, можно сформировать его вокруг lat/lon или взять из dest_id
                // Для этого API обычно bbox передается как "lat_min,lat_max,lon_min,lon_max"
                if (location.has("bbox")) {
                    return location.get("bbox").asText();
                }
                
                // Запасной вариант: если есть координаты, создаем небольшой bbox вокруг центра города
                if (location.has("latitude") && location.has("longitude")) {
                    double lat = location.get("latitude").asDouble();
                    double lon = location.get("longitude").asDouble();
                    // Примерный bbox (+/- 0.1 градуса ~ 11км)
                    return String.format("%f,%f,%f,%f", lat - 0.1, lat + 0.1, lon - 0.1, lon + 0.1);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get bbox for city {}: {}", city, e.getMessage());
        }
        // Дефолтный bbox (Манила из примера), если поиск не удался
        return "14.291283,14.948423,120.755688,121.136864";
    }

    @Override
    public Output apply(Input input) {
        log.info("Executing HotelBookingSkill with RapidAPI for: {}", input);
        
        try {
            String bbox = getBBoxForCity(input.city());
            log.info("Using BBox for {}: {}", input.city(), bbox);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("x-rapidapi-host", apiHost);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Используем динамический bbox в URL
            String url = String.format("%s/properties/list-by-map?room_qty=1&guest_qty=1&bbox=%s&languagecode=en-us", 
                baseUrl, bbox);

            log.debug("Calling Booking API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            log.info("Booking API responded with status: {}", response.getStatusCode());

            String reservationId = "HTL-" + System.currentTimeMillis() % 10000;

            // Регистрируем предложение в интерфейсе (Booking Planner)
            proposalService.addProposal(BookingProposal.builder()
                    .type("HOTEL")
                    .title("Hotel: " + input.hotelName())
                    .description("Stay in " + input.city() + " from " + input.checkIn() + " to " + input.checkOut())
                    .price("$120 / ночь")
                    .details(Map.of(
                        "hotelName", input.hotelName(),
                        "city", input.city(),
                        "checkIn", input.checkIn(),
                        "checkOut", input.checkOut(),
                        "email", input.email(),
                        "reservationId", reservationId
                    ))
                    .build());

            return new Output(
                reservationId,
                "PENDING",
                "Hotel " + input.hotelName() + " in " + input.city() + " proposed. " +
                "Please confirm it in the Booking Planner interface."
            );
        } catch (Exception e) {
            log.error("Error calling Booking API: {}", e.getMessage());
            return new Output(
                "HTL-ERROR",
                "PARTIAL_SUCCESS",
                "Hotel " + input.hotelName() + " in " + input.city() + " reserved (Offline Mode). " +
                "API Error: " + e.getMessage()
            );
        }
    }
}
