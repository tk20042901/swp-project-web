package com.swp.project.service.user;

import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.CreateManagerDto;
import com.swp.project.dto.ViewManagerDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.user.Manager;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@RequiredArgsConstructor
@Service
public class ManagerService {
    private final ManagerRepository managerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CommuneWardRepository communeWardRepository;
    private final SellerRepository sellerRepository;
    private final ShipperRepository shipperRepository;
    public Manager getManagerById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    public void updateManager(Long id, EditManagerDto updatedManager) {
        Manager existingManager = managerRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("Không tìm thấy quản lý")
        );
        CommuneWard ward = communeWardRepository.findById(updatedManager.getCommuneWardCode())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xã"));
        boolean isEnabled = Boolean.TRUE.equals(updatedManager.getStatus());
        if (!existingManager.getEmail().equals(updatedManager.getEmail()) && userRepository.existsByEmail(updatedManager.getEmail())) {
            throw new IllegalArgumentException("Mail đã được sử dụng");
        }
        if(!existingManager.getCid().equals(updatedManager.getCId()) 
            && (sellerRepository.findByCid(updatedManager.getCId()) != null ||
                shipperRepository.findByCid(updatedManager.getCId()) != null ||
                managerRepository.findByCid(updatedManager.getCId()) != null)) {
            throw new IllegalArgumentException("Căn cước công dân đã được sử dụng");
        }
        
        if(!updatedManager.getPassword().isBlank()){
            existingManager.setPassword(passwordEncoder.encode(updatedManager.getPassword()));
        }
        existingManager.setEmail(updatedManager.getEmail());
        existingManager.setFullname(updatedManager.getFullname());
        existingManager.setBirthDate(updatedManager.getBirthDate());
        existingManager.setCid(updatedManager.getCId());
        existingManager.setSpecificAddress(updatedManager.getSpecificAddress());
        existingManager.setCommuneWard(ward);
        existingManager.setEnabled(isEnabled);
        if(!isEnabled) eventPublisher.publishEvent(new UserDisabledEvent(existingManager.getEmail()));
        managerRepository.save(existingManager);
    }
    public void validateCreateManager(CreateManagerDto registerDto, BindingResult bindingResult) throws RuntimeException{
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Mail đã được sử dụng");
        }
        if (bindingResult.hasErrors()) {
                FieldError fieldError = bindingResult.getFieldErrors().get(0);
                String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
                throw new RuntimeException(message);
        }
    }
    public void createManager(CreateManagerDto registerDto) {
        CommuneWard ward = communeWardRepository.findById(registerDto.getCommuneWardCode())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xã"));
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Mail đã được sử dụng");
        }
        if(sellerRepository.findByCid(registerDto.getCId()) != null ||
           shipperRepository.findByCid(registerDto.getCId()) != null ||
           managerRepository.findByCid(registerDto.getCId()) != null) {
            throw new IllegalArgumentException("Căn cước công dân đã được sử dụng");
        }
        Manager manager = Manager.builder()
            .email(registerDto.getEmail())
            .password(passwordEncoder.encode(registerDto.getPassword()))
            .fullname(registerDto.getFullname())
            .birthDate(registerDto.getBirthDate())
            .cid(registerDto.getCId())
            .communeWard(ward)
            .specificAddress(registerDto.getSpecificAddress())
            .build();
        managerRepository.save(manager);
    }

    public List<ViewManagerDto> getAllViewManager(){
        return managerRepository.findAll().stream()
            .map(m -> new ViewManagerDto(m.getId(), m.getEmail(),m.isEnabled()))
            .toList();
    }

   
    
}
