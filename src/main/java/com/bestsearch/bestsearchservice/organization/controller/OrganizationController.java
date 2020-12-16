package com.bestsearch.bestsearchservice.organization.controller;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationInputDTO;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

	private final OrganizationService organizationService;

	public OrganizationController(final OrganizationService organizationService) {
		this.organizationService = organizationService;
	}

	@GetMapping("/type")
	public ResponseEntity<List<OrganizationOutputDTO>> getAllOrganizationsByType(@RequestParam ("typeId") long typeId) {
		return ResponseEntity.ok(this.organizationService.getActiveOrganizationsByType(typeId));
	} 
	
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationOutputDTO> getOrganizationById(@PathVariable("id") long id) {
		return ResponseEntity.ok(this.organizationService.getOrganizationById(id));
	} 
	
	@PostMapping
	public ResponseEntity<OrganizationOutputDTO> addOrganization(@RequestBody OrganizationInputDTO organizationInputDTO) {
		return new ResponseEntity<>(this.organizationService.addOrganization(organizationInputDTO),  HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<OrganizationOutputDTO> updateOrganizationById(@PathVariable("id") long id,
			@RequestBody OrganizationInputDTO organizationInputDTO) {
		return ResponseEntity.ok(this.organizationService.updateOrganization(id, organizationInputDTO));
	}

	@GetMapping("/hello")
	public String sayHello(){
		List<OrganizationOutputDTO> organizationOutputDTOs = organizationService.getOrderedActiveOrganizationsWithinRadius(61.21759217, -149.8935557,0);


		int index = 1;
		List<OrderAssignment> newAssignments = new ArrayList<>();
		for(OrganizationOutputDTO org : organizationOutputDTOs) {
			newAssignments.add(OrderAssignment.builder()
					.orderId(1l)
					.organizationId(org.getId())
					.assignedAt(index == 1 ? LocalDateTime.now() : null)
					.assignedStatus(index == 1 ? Status.PENDING : Status.INITIAL)
					.orderType(OrderType.CLOSEST)
					.priority(index)
					.offsetPaginate(0)
					.build());
			index++;
		}

		System.out.println("Dummy endpoint");
		newAssignments.forEach(System.out::println);
		return "Hello...";
	}
}
