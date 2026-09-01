package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.model.Client;
import com.example.microfinance_loan_system.model.Group;
import com.example.microfinance_loan_system.repository.ClientRepository;
import com.example.microfinance_loan_system.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Group createGroup(List<Long> clientIds) {
        if (clientIds == null || clientIds.size() < 2) {
            throw new IllegalArgumentException("A group must have at least 2 clients.");
        }

        // Validate all clients exist
        List<Client> clients = clientRepository.findAllById(clientIds);
        if (clients.size() != clientIds.size()) {
            throw new IllegalArgumentException("One or more client IDs are invalid.");
        }

        // Create the group
        Group group = new Group();
        group.setName("Group " + System.currentTimeMillis());
        Group savedGroup = groupRepository.save(group);

        // Update clients to belong to this group
        for (Client client : clients) {
            client.setGroupId(savedGroup.getId());
        }
        clientRepository.saveAll(clients);

        return savedGroup;
    }

    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
    }
}
