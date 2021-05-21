package com.bestsearch.bestsearchservice.order.service;

import com.bestsearch.bestsearchservice.order.dto.*;
import com.bestsearch.bestsearchservice.order.model.Order;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.order.producer.OrderAssignmentProducer;
import com.bestsearch.bestsearchservice.order.repository.OrderRepository;
import com.bestsearch.bestsearchservice.order.utils.IdentifierGenerator;
import com.bestsearch.bestsearchservice.order.utils.OrderDateFormatter;
import com.bestsearch.bestsearchservice.orderPrice.model.OrderPrice;
import com.bestsearch.bestsearchservice.orderPrice.service.OrderPriceService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;
import com.bestsearch.bestsearchservice.share.exception.OperationAbortedException;
import com.bestsearch.bestsearchservice.share.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

  private static final String YEAR_MONTH_KEY = "YEARMONTH";
  private static final String ORDERID = "ORDID";
  private static final String YEAR_MONTH_DATE_PATTERN = "ddMMyy";
  private final String orderRefPattern;

  private final OrderRepository orderRepository;

  private final OrderAssignmentProducer producer;

  private final String orderSqsName;

  private final OrganizationService organizationService;

  private final OrderPriceService orderPriceService;

  public OrderServiceImpl(final OrderRepository orderRepository,
      final OrderAssignmentProducer producer,
      final @Value("${order.ref.pattern}") String orderRefPattern,
      final @Value("${aws.sqs.order}") String orderSqsName,
      final OrganizationService organizationService,
      final OrderPriceService orderPricerService) {
    this.orderRepository = orderRepository;
    this.orderRefPattern = orderRefPattern;
    this.producer = producer;
    this.orderSqsName = orderSqsName;
    this.organizationService = organizationService;
    this.orderPriceService = orderPricerService;
  }

  @Override
  public OrderOutputDTO saveOrder(OrderCreateDTO orderCreateDTO) {
    long id = orderRepository.getNextId();
    String orderRef = IdentifierGenerator.generateIdentifier(
        Map.of(ORDERID, id, YEAR_MONTH_KEY,
            LocalDate.now().format(DateTimeFormatter.ofPattern(YEAR_MONTH_DATE_PATTERN))),
        orderRefPattern);

    Order toBeSaved = Order.builder()
        .id(id)
        .latitude(orderCreateDTO.getLatitude())
        .longitude(orderCreateDTO.getLongitude())
        .orderRef(orderRef)
        .status(Status.SEARCHING)
        .orderType(orderCreateDTO.getOrderType())
        .organizationId(orderCreateDTO.getOrderType() == OrderType.PREFERRED ? orderCreateDTO
            .getOrganizationId() : 0)
        .organizationTypeId(orderCreateDTO.getOrganizationTypeId())
        .userId(orderCreateDTO.getUserId())
        .orderedAt(LocalDateTime.now())
        .images(orderCreateDTO.getImages())
        .userComment(orderCreateDTO.getUserComment())
        .alternateDrugsAllowed(orderCreateDTO.getAlternateDrugsAllowed())
        .build();

    OrderOutputDTO savedOrder = orderRepository.save(toBeSaved).viewAsOrderOutputDTO();
    producer.send(savedOrder);

    return savedOrder;
  }

  @Override
  public OrderOutputDTO getOrderById(long id) {
    return orderRepository.findById(id)
        .map(Order::viewAsOrderOutputDTO)
        .map(this::fillOrderPrice)
        .orElseThrow(() -> new ResourceNotFoundException("No order found"));
  }

  private OrderOutputDTO fillOrderPrice(OrderOutputDTO orderOutputDTO) {
    try {
      OrderPrice orderPrice = orderPriceService.getOrderPriceByOrderID(orderOutputDTO.getId());
      orderOutputDTO.setOriginalPrice(orderPrice.getOriginalPrice());
      orderOutputDTO.setAlternatePrice(orderPrice.getAlternatePrice());
      orderOutputDTO.setAdditionalDetails(orderPrice.getAdditionalDetails());
    } catch (Exception e) {

    }
    return orderOutputDTO;

  }

  private List<OrderOutputDTO> fillOrderPrice(List<OrderOutputDTO> orderOutputDTOs) {
    List<Long> orderOutputIds = orderOutputDTOs.stream().map(OrderOutputDTO::getId)
        .collect(Collectors.toUnmodifiableList());
    try {
      Map<Long, OrderPrice> orderPrices = orderPriceService
          .getOrderPricesByOrderIDs(orderOutputIds);
      return orderOutputDTOs.stream().map(orderOutputDTO -> {
        OrderPrice orderPrice = orderPrices.get(orderOutputDTO.getId());
        if (null != orderPrice) {
          orderOutputDTO.setOriginalPrice(orderPrice.getOriginalPrice());
          orderOutputDTO.setAlternatePrice(orderPrice.getAlternatePrice());
          orderOutputDTO.setAdditionalDetails(orderPrice.getAdditionalDetails());
        }

        return orderOutputDTO;
      }).collect(Collectors.toUnmodifiableList());
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public List<OrderOutputDTO> getOrdersById(Set<Long> ids) {
    List<OrderOutputDTO> orderOutputDTOS = orderRepository.findAllById(ids).stream()
        .map(Order::viewAsOrderOutputDTO).collect(Collectors.toList());

    return this.fillOrderPrice(orderOutputDTOS);
  }

  @Override
  public OrderOutputDTO getOrderByRef(String orderRef, long organizationTypeId) {
    return orderRepository.findByOrderRefAndOrganizationTypeId(orderRef, organizationTypeId)
        .map(Order::viewAsOrderOutputDTO)
        .map(this::fillOrderPrice)
        .orElseThrow(() -> new ResourceNotFoundException("No order found"));
  }

  @Override
  public void changeOrderStatus(long id, Status toStatus) {
    orderRepository.updateOrderStatus(id, toStatus);
  }

  @Transactional
  @Override
  public void changeOrderStatusAndOrganization(long id, Status toStatus, long organizationId) {
    orderRepository.updateOrderStatusAndOrganization(id, toStatus, organizationId);
  }

  @Override
  public OrderOutputDTO updateOrder(long id, OrderInputDTO orderInputDTO) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No data found"));
    if (!this.isOrderChanged(order, orderInputDTO)) {
      throw new OperationAbortedException("No changes requested");
    }

    this.updateOrder(order, orderInputDTO);
    OrderOutputDTO savedOrder = orderRepository.save(order).viewAsOrderOutputDTO();
    producer.send(savedOrder);

    return savedOrder;
  }

  @Override
  public List<OrderOutputDTO> getOrders() {
    List<OrderOutputDTO> orderOutputDTOS = orderRepository.findAll().stream()
        .map(Order::viewAsOrderOutputDTO).collect(Collectors.toList());

    return this.fillOrderPrice(orderOutputDTOS);


  }

  @Override
  public List<OrderAndPeriodDTO> getCurrentOrders(long orgTypeId, long userId) {
    List<OrderOutputDTO> orderOutputDTOS = orderRepository
        .getOrdersByStatues(orgTypeId, userId, List.of(Status.SEARCHING, Status.ACCEPTED))
        .orElseThrow(() -> new ResourceNotFoundException("No data found"))
        .stream().map(Order::viewAsOrderOutputDTO)
        .collect(Collectors.toList());

    List<OrderOutputDTO> orderDTOsWithPrice = this.fillOrderPrice(orderOutputDTOS);
    return getOrderAndPeriodDTOS(orderDTOsWithPrice);

  }

  @Override
  public List<OrderAndPeriodDTO> getPastOrders(long orgTypeId, long userId) {
    List<OrderOutputDTO> orderOutputDTOS = orderRepository
        .getOrdersByStatues(orgTypeId, userId, List.of(Status.CANCELLED, Status.COMPLETED))
        .orElseThrow(() -> new ResourceNotFoundException("No data found"))
        .stream().map(Order::viewAsOrderOutputDTO)
        .collect(Collectors.toList());

    List<OrderOutputDTO> orderDTOsWithPrice = this.fillOrderPrice(orderOutputDTOS);
    return getOrderAndPeriodDTOS(orderDTOsWithPrice);
  }

  private List<OrderAndPeriodDTO> getOrderAndPeriodDTOS(List<OrderOutputDTO> orderOutputDTOS) {
    List<Long> organizationIds = orderOutputDTOS.stream()
        .map(OrderOutputDTO::getOrganizationId)
        .filter((id) -> Objects.nonNull(id) && id > 0)
        .collect(Collectors.toList());

    if (Objects.nonNull(organizationIds) && !organizationIds.isEmpty()) {
      Map<Long, OrganizationOutputDTO> organizationOutputDTOS = organizationService
          .getOrganizationByIds(organizationIds).stream()
          .collect(Collectors.toMap(OrganizationOutputDTO::getId, Function.identity()));

      orderOutputDTOS.forEach((orderOutputDTO) -> {
        OrganizationOutputDTO matchedOrganization = organizationOutputDTOS
            .get(orderOutputDTO.getOrganizationId());
        if (Objects.nonNull(matchedOrganization)) {
          orderOutputDTO.setOrganizationDTO(OrganizationDTO.builder()
              .id(matchedOrganization.getId())
              .name(matchedOrganization.getName())
              .address(matchedOrganization.getAddress())
              .latitude(matchedOrganization.getLatitude())
              .longitude(matchedOrganization.getLongitude()).build());
        }
      });
    }

    return orderOutputDTOS.stream()
        .collect(Collectors.groupingBy(OrderOutputDTO::getOrderDate))
        .entrySet().stream()
        .sorted(
            Map.Entry.<LocalDate, List<OrderOutputDTO>>comparingByKey(Comparator.reverseOrder()))
        .map(o -> new OrderAndPeriodDTO(OrderDateFormatter.formatForUI(o.getKey()), o.getValue()))
        .collect(Collectors.toList());
  }

  private void updateOrder(Order order, OrderInputDTO orderInputDTO) {
    order.setUserComment(orderInputDTO.getUserComment());
    if (orderInputDTO.getStatus() == Status.CANCELLED
        || orderInputDTO.getStatus() == Status.COMPLETED) {
      order.setStatus(orderInputDTO.getStatus());
    }
  }

  private boolean isOrderChanged(Order order, OrderInputDTO orderInputDTO) {
    if (order.getStatus() == orderInputDTO.getStatus()
        && order.getUserComment().trim().equals(orderInputDTO.getUserComment())) {
      return false;
    }

    return true;
  }
}
