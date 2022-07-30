package io.adnanedu.ppmtool.services;

import io.adnanedu.ppmtool.domain.Backlog;
import io.adnanedu.ppmtool.domain.Project;
import io.adnanedu.ppmtool.domain.User;
import io.adnanedu.ppmtool.dto.ProjectDto;
import io.adnanedu.ppmtool.exceptions.ProjectIdException;
import io.adnanedu.ppmtool.exceptions.ProjectNotFoundException;
import io.adnanedu.ppmtool.repositories.BacklogRepository;
import io.adnanedu.ppmtool.repositories.ProjectRepository;
import io.adnanedu.ppmtool.repositories.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    BacklogRepository backlogRepository;

    @Autowired
    UserRepository userRepository;

    public Project saveOrUpdateProject(ProjectDto projectDto, String userName){
        Project project = new Project();
        BeanUtils.copyProperties(projectDto, project);

        if(project.getId() != null){
            Project existingProject = projectRepository.findProjectByProjectIdentifier(project.getProjectIdentifier());
            if(existingProject !=null &&(!existingProject.getProjectLeader().equals(userName))){
                throw new ProjectNotFoundException("Project not found in your account");
            }else if(existingProject == null){
                throw new ProjectNotFoundException("Project with ID: '"+project.getProjectIdentifier()+"' cannot be updated because it doesn't exist");
            }
        }

        try{
            User user = userRepository.findByUsername(userName);
            project.setUser(user);
            project.setProjectLeader(user.getUsername());

            project.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());

            if(project.getId()==null){
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());
            }
            if(project.getId()!=null){
                project.setBacklog(backlogRepository.findBacklogByProjectIdentifier(project.getProjectIdentifier().toUpperCase()));
            }
            return projectRepository.save(project);
        }catch (Exception e){
            throw new ProjectIdException("Project ID '"+project.getProjectIdentifier().toUpperCase()+"' already exists");
        }
    }
    public ProjectDto findProjectByIdentifier(String projectId, String userName){
        Project project = projectRepository.findProjectByProjectIdentifier(projectId.toUpperCase());
        if(project == null){
            throw new ProjectIdException("Project ID '"+projectId+"' does not exist");
        }
        if(!project.getProjectLeader().equals(userName)){
            throw new ProjectNotFoundException("Project not found in your account");
        }


        ProjectDto projectDto = new ProjectDto();
        BeanUtils.copyProperties(project, projectDto);
        return projectDto;
    }
    public Iterable<ProjectDto> findAllProjects(String userName){
        Iterable<Project> projects = projectRepository.findAllByProjectLeader(userName);
        List<ProjectDto> projectDtoList = new ArrayList<>();
        for(Project project: projects){
            ProjectDto projectDto = new ProjectDto();
            BeanUtils.copyProperties(project, projectDto);
            projectDtoList.add(projectDto);
        }
        return projectDtoList;
    }
    public void deleteProjectByIdentifier(String projectid, String userName){
        ProjectDto projectDto = findProjectByIdentifier(projectid, userName);
        Project project = new Project();
        BeanUtils.copyProperties(projectDto, project);
        projectRepository.delete(project);
    }
}
