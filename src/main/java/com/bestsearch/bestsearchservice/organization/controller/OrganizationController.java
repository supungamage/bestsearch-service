package com.bestsearch.bestsearchservice.organization.controller;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationInputDTO;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;
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
		return "Hello...";
	}
}
