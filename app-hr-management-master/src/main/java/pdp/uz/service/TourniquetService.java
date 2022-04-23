package pdp.uz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pdp.uz.entity.Employee;
import pdp.uz.entity.TourniquetCard;
import pdp.uz.entity.TourniquetHistory;
import pdp.uz.payload.ApiResponse;
import pdp.uz.payload.TourniquetCardDto;
import pdp.uz.payload.TourniquetHistoryDto;
import pdp.uz.repository.EmployeeRepository;
import pdp.uz.repository.TourniquetHistoryRepository;
import pdp.uz.repository.TourniquetRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class TourniquetService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    TourniquetRepository tourniquetRepository;
    @Autowired
    TourniquetHistoryRepository tourniquetHistoryRepository;

    public ApiResponse create(TourniquetCardDto dto) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(dto.getEmployeeEmail());
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        Employee employee = optionalEmployee.get();
        TourniquetCard card = new TourniquetCard();
        card.setCompany(employee.getCompany());
        card.setEmployee(employee);
        tourniquetRepository.save(card);
        return new ApiResponse("Tourniquet card is successfully created", true);
    }


    public ApiResponse edit(TourniquetCardDto dto, String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(dto.getEmployeeEmail());
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        Optional<TourniquetCard> optionalTourniquetCard = tourniquetRepository.findByEmployee_EmailAndStatusTrue(email);
        if (optionalTourniquetCard.isPresent()) {
            TourniquetCard card = optionalTourniquetCard.get();
            card.setEmployee(optionalEmployee.get());
            tourniquetRepository.save(card);
            return new ApiResponse("Card updated", true);
        }
        return new ApiResponse("Card not found", false);
    }

    public ApiResponse activate(TourniquetHistoryDto dto) {
        Optional<TourniquetCard> optionalTourniquetCard =
                tourniquetRepository.findById(UUID.fromString(dto.getCardId()));
        if (optionalTourniquetCard.isPresent()) {
            TourniquetCard card = optionalTourniquetCard.get();
            card.setStatus(true);
            card.setExpireDate(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 3));
            tourniquetRepository.save(card);
            return new ApiResponse("Card activated", true);
        }
        return new ApiResponse("Card not found", true);
    }


    public ApiResponse enter(TourniquetHistoryDto dto) {
        Optional<TourniquetCard> cardOptional = tourniquetRepository.findById(UUID.fromString(dto.getCardId()));
        if (!cardOptional.isPresent()) {
            return new ApiResponse("Card not found", false);
        }
        TourniquetCard card = tourniquetRepository.save(checkActive(cardOptional.get()));
        if (card.isStatus()) {
            TourniquetHistory tourniquetHistory = new TourniquetHistory();
            tourniquetHistory.setEnteredAt(Timestamp.valueOf(LocalDateTime.now()));
            tourniquetHistory.setTourniquetCard(card);
            tourniquetHistoryRepository.save(tourniquetHistory);
            return new ApiResponse("Entered", true);
        }
        return new ApiResponse("Expiration date of the card", false);
    }


    public TourniquetCard checkActive(TourniquetCard card) {
        if (card.getExpireDate().before(new Date())) {
            card.setStatus(false);
        }
        return card;
    }


    public ApiResponse exit(TourniquetHistoryDto dto) {
        Optional<TourniquetCard> cardOptional = tourniquetRepository.findById(UUID.fromString(dto.getCardId()));
        if (!cardOptional.isPresent()) {
            return new ApiResponse("Card not found", false);
        }
        TourniquetCard card = tourniquetRepository.save(checkActive(cardOptional.get()));
        if (card.isStatus()) {
            TourniquetHistory tourniquetHistory = new TourniquetHistory();
            tourniquetHistory.setExitedAt(Timestamp.valueOf(LocalDateTime.now()));
            tourniquetHistory.setTourniquetCard(card);
            tourniquetHistoryRepository.save(tourniquetHistory);
            return new ApiResponse("Exited", true);
        }
        return new ApiResponse("Expiration date of the card", false);
    }

    public ApiResponse delete(String id) {
        Optional<TourniquetCard> optionalTourniquetCard =
                tourniquetRepository.findById(UUID.fromString(id));
        if (!optionalTourniquetCard.isPresent()) {
            return new ApiResponse("Card not found", true);
        }
        TourniquetCard card = optionalTourniquetCard.get();
        card.setStatus(false);
        tourniquetRepository.save(card);
        return new ApiResponse("Card deleted", true);
    }
}
