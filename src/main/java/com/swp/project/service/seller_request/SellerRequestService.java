package com.swp.project.service.seller_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.repository.seller_request.SellerRequestRepository;
import com.swp.project.service.user.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
public class SellerRequestService {

    private final ObjectMapper objectMapper;
    private final SellerRequestRepository sellerRequestRepository;
    private final SellerRequestTypeService sellerRequestTypeService;
    private final SellerRequestStatusService sellerRequestStatusService;
    private final SellerService sellerService;

    public List<SellerRequest> getAllSellerRequest() {
        return sellerRequestRepository.findAll();
    }

    public List<SellerRequest> getASellerRequestsOrderByDateDesc(){
        return sellerRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SellerRequest> getSellerRequestByEntityName(Class<?> entityClass) {
        return sellerRequestRepository.findByEntityName(entityClass.getSimpleName());
    }

    public List<SellerRequest> getSellerRequestBySellerEmail(String email) {
        return sellerRequestRepository.findBySellerEmail(email);
    }

    public SellerRequest getSellerRequestById(Long id) {
        return sellerRequestRepository.findById(id).orElse(null);
    }

    public <T> void saveAddRequest(T entity, String sellerEmail) throws JsonProcessingException {
        sellerRequestRepository.save(SellerRequest.builder()
                .entityName(entity.getClass().getSimpleName())
                .content(objectMapper.writeValueAsString(entity))
                .seller(sellerService.getSellerByEmail(sellerEmail))
                .requestType(sellerRequestTypeService.getAddType())
                .status(sellerRequestStatusService.getPendingStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }

    public <T> void saveDeleteRequest(T entity, String sellerEmail) throws JsonProcessingException {
        sellerRequestRepository.save(SellerRequest.builder()
                .entityName(entity.getClass().getSimpleName())
                .content(objectMapper.writeValueAsString(entity))
                .seller(sellerService.getSellerByEmail(sellerEmail))
                .requestType(sellerRequestTypeService.getDeleteType())
                .status(sellerRequestStatusService.getPendingStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }

    public <T> void updatePendingRequestContent(Long requestId, T newContent) throws JsonProcessingException {
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        if (sellerRequest != null && sellerRequestStatusService.isPendingStatus(sellerRequest)) {
            sellerRequest.setContent(objectMapper.writeValueAsString(newContent));
            sellerRequestRepository.save(sellerRequest);
        }
    }

    public <T> void  saveUpdateRequest(T oldEntity, T entity, String sellerEmail) throws JsonProcessingException {
        sellerRequestRepository.save(SellerRequest.builder()
                .entityName(entity.getClass().getSimpleName())
                .oldContent(objectMapper.writeValueAsString(oldEntity))
                .content(objectMapper.writeValueAsString(entity))
                .seller(sellerService.getSellerByEmail(sellerEmail))
                .requestType(sellerRequestTypeService.getUpdateType())
                .status(sellerRequestStatusService.getPendingStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }

    public <T> T getEntityFromContent(String content, Class<T> entityClass) throws JsonProcessingException {
        return objectMapper.readValue(content, entityClass);
    }

    public <T> SellerRequest approveRequest(Long requestId, Class<T> entityClass, Consumer<T> addFunction,
            Consumer<T> updateFunction) throws Exception {
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        sellerRequest.setStatus(sellerRequestStatusService.getApprovedStatus());
        sellerRequestRepository.save(sellerRequest);
        String requestTypeName = sellerRequest.getRequestType().getName();
        String requestContent = sellerRequest.getContent();

        if (requestTypeName.equals(sellerRequestTypeService.getAddType().getName())) {
            executeAddRequest(requestContent, entityClass, addFunction);
        } else if (requestTypeName.equals(sellerRequestTypeService.getUpdateType().getName())) {
            executeUpdateRequest(requestContent, entityClass, updateFunction);
        }
        return sellerRequest;
    }

    public <T> void approveDeleteRequest(Long requestId, Class<T> entityClass, Consumer<T> deleteFunction)
            throws Exception {
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        sellerRequest.setStatus(sellerRequestStatusService.getApprovedStatus());
        sellerRequestRepository.save(sellerRequest);
        String requestContent = sellerRequest.getContent();
        T entity = objectMapper.readValue(requestContent, entityClass);
        deleteFunction.accept(entity);
    }

    public void rejectRequest(Long requestId) {
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        sellerRequest.setStatus(sellerRequestStatusService.getRejectedStatus());
        sellerRequestRepository.save(sellerRequest);
    }

    public <T> void executeAddRequest(String requestContent, Class<T> clazz, Consumer<T> addFunction)
            throws JsonProcessingException {
        T entity = objectMapper.readValue(requestContent, clazz);
        addFunction.accept(entity);
    }

    public <T> void executeUpdateRequest(String requestContent, Class<T> clazz, Consumer<T> updateFunction)
            throws JsonProcessingException {
        T entity = objectMapper.readValue(requestContent, clazz);
        updateFunction.accept(entity);
    }

    public <T> void updateOldContent(T oldProduct, SellerRequest sellerRequest) throws JsonProcessingException {
        sellerRequest.setOldContent(objectMapper.writeValueAsString(oldProduct));
        sellerRequestRepository.save(sellerRequest);
    }
}
