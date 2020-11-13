package com.bestsearch.bestsearchservice.organization.controller;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeInputDTO;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/organization-types")
public class OrganizationTypeController { 
	
	private final OrganizationTypeService organizationTypeService;

	public OrganizationTypeController(OrganizationTypeService organizationTypeService) {
		this.organizationTypeService = organizationTypeService;
	}
	
	@GetMapping
	public ResponseEntity<List<OrganizationTypeOutputDTO>> getAllOrganizationTypes() {
		return ResponseEntity.ok(this.organizationTypeService.getOrganizationTypes());
	} 
	
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationTypeOutputDTO> getOrganizationTypeById(@PathVariable("id") long id) {
		return ResponseEntity.ok(this.organizationTypeService.getOrganizationTypeById(id));
	} 
	
	@PostMapping
	public ResponseEntity<OrganizationTypeOutputDTO> addOrganizationType(@RequestBody OrganizationTypeInputDTO organizationTypeInputDTO) {
		return new ResponseEntity<>(this.organizationTypeService.addOrganizationType(organizationTypeInputDTO),  HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<OrganizationTypeOutputDTO> updateOrganizationTypeById(@PathVariable("id") long id, 
			@RequestBody OrganizationTypeInputDTO organizationTypeInputDTO) {
		return ResponseEntity.ok(this.organizationTypeService.updateOrganizationType(id, organizationTypeInputDTO));
	}

	@GetMapping("/hello")
	public String sayHello(){
		return "Hello...";
	}
}
