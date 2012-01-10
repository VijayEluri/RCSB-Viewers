package org.rcsb.mbt.surface.gamer;

import org.rcsb.mbt.surface.gamer.biom.ATOM;
import org.rcsb.mbt.surface.gamer.biom.EIGENVECT;
import org.rcsb.mbt.surface.gamer.biom.FLTVECT;
import org.rcsb.mbt.surface.gamer.biom.NPNT3;
import org.rcsb.mbt.surface.gamer.biom.SurfMesh;

/*
 * ***************************************************************************
 * GAMER = < Geometry-preserving Adaptive MeshER >
 * Copyright (C) 1994-- Michael Holst and Zeyun Yu
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ***************************************************************************
 */


/* ***************************************************************************
 * File:     SurfSmooth.C   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Smooth and coarsen surface triangular meshes
 * ****************************************************************************
 */

public final class SurfSmooth {
	private static biom biom = new biom();

/*
 * ***************************************************************************
 * Routine:  GenerateHistogram   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Generate the angle distribution (0 - 180 degrees)
 * ***************************************************************************
 */
public static void GenerateHistogram(SurfMesh surfmesh)
{
  int n,m;
  int a,b,c;
  float angle;
  int[] histogram = new int[18];

  for (m = 0; m < 18; m++)
    histogram[m] = 0;

  for (n = 0; n < surfmesh.nf; n++) {
    a = surfmesh.face[n].a;
    b = surfmesh.face[n].b;
    c = surfmesh.face[n].c;

    angle = GetAngleSurfaceOnly(surfmesh,a,b,c);
    for (m=0; m<18; m++) {
      if (angle >= m*10 && angle < m*10+10)
	histogram[m] += 1;
    }

    angle = GetAngleSurfaceOnly(surfmesh,b,a,c);
    for (m=0; m<18; m++) {
      if (angle >= m*10 && angle < m*10+10)
        histogram[m] += 1;
    }

    angle = GetAngleSurfaceOnly(surfmesh,c,a,b);
    for (m=0; m<18; m++) {
      if (angle >= m*10 && angle < m*10+10)
        histogram[m] += 1;
    }
   
  }
  
  for (m = 0; m < 18; m++) {
    System.out.printf("%f  ",100.0*(float)histogram[m]/((float)surfmesh.nf*3.0));
  }
  System.out.printf("\n\n");
}

/*
 * ***************************************************************************
 * Routine:  SurfaceMesh_smooth   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Function for surface smoothing
 * ***************************************************************************
 */
public static boolean SurfaceMesh_smooth(SurfMesh surfmesh, 
		 int max_min_angle, 
		 int min_max_angle, 
		 int max_iter,
		boolean flip_edges)
{
  Float min_angle = new Float(0);
  Float max_angle = new Float(0);
  Integer num_small = new Integer(0);
  Integer num_large = new Integer(0);
  int i, n;
  boolean smoothed = false;

  // Check if neighborlist is created
  if (surfmesh.neighbor_list == null){
    SurfaceMesh.SurfaceMesh_createNeighborlist(surfmesh);
  }
  i = 0;

  // Print the initial quality only when doing 1 or more iterations
  if (max_iter > 1) {
//    SurfaceMesh_getMinMaxAngles(surfmesh, &min_angle, &max_angle, &num_small, 
//			       &num_large, max_min_angle, min_max_angle);
	  // passing min_angle, max_angle, num_small, num_large as objects (by reference)
	  SurfaceMesh_getMinMaxAngles(surfmesh, min_angle, max_angle, num_small, 
		       num_large, max_min_angle, min_max_angle);
    
    System.out.printf("%2d: min_angle: %f - max_angle: %f - " +
	   "smaller-than-%d: %d - larger-than-%d: %d\n", 
	   i, min_angle, max_angle, max_min_angle, num_small, 
	   min_max_angle, num_large);

  }

  while (!smoothed && i < max_iter)
  {
    
    i++;
    
    // Smooth all vertices
    if (flip_edges){
      for (n = 0; n < surfmesh.nv; n++){
	MoveVerticesSurfaceOnly(surfmesh, n);
	EdgeFlipping(surfmesh, n);
      }
    }
    else{
      for (n = 0; n < surfmesh.nv; n++) 
	MoveVerticesSurfaceOnly(surfmesh, n);
    }

    // Calculate and print quality after surface smooth
//    SurfaceMesh_getMinMaxAngles(surfmesh, &min_angle, &max_angle, &num_small, 
//			       &num_large, max_min_angle, min_max_angle);
    // using Float and Integer instead of primitive types to pass by reference
    SurfaceMesh_getMinMaxAngles(surfmesh, min_angle, max_angle, num_small, 
		       num_large, max_min_angle, min_max_angle);
    
    // Print the iteration number only when doing 1 or more iterations
    if (max_iter != 1)
      System.out.printf("%2d: min_angle: %f - max_angle: %f - " +
	     "smaller-than-%d: %d - larger-than-%d: %d\n", 
	     i, min_angle, max_angle, max_min_angle, num_small, 
	     min_max_angle, num_large);
    else
      System.out.printf("    min_angle: %f - max_angle: %f - " +
	     "smaller-than-%d: %d - larger-than-%d: %d\n", 
	     min_angle, max_angle, max_min_angle, num_small, 
	     min_max_angle, num_large);
      
    // Check if the mesh is smoothed
    smoothed = min_angle > max_min_angle && max_angle < min_max_angle;
  }

  return smoothed;
}

/*
 * ***************************************************************************
 * Routine:  SurfaceMesh_normalSmooth
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Function for surface smoothing using normal smoothing
 * ***************************************************************************
 */
public static void SurfaceMesh_normalSmooth(SurfMesh surfmesh)
{
   int n;
  Float min_angle = new Float(0);
  Float max_angle = new Float(0);
  Integer num_small = new Integer(0);
  Integer num_large = new Integer(0);
  
  // Check if neighborlist is created
//  if (!surfmesh.neighbor_list)
	if (surfmesh.neighbor_list == null)
         SurfaceMesh.SurfaceMesh_createNeighborlist(surfmesh);

  // Normal smooth all vertices
  for (n = 0; n < surfmesh.nv; n++) 
    NormalSmooth(surfmesh, n);

 // SurfaceMesh_getMinMaxAngles(surfmesh, &min_angle, &max_angle, 
//			     &num_small, &num_large, 15, 150);
  // using Float and Integer instead of primitive types to pass by reference
  SurfaceMesh_getMinMaxAngles(surfmesh, min_angle, max_angle, 
		     num_small, num_large, 15, 150);
  System.out.printf("    min_angle: %f - max_angle: %f - smaller-than-15: %d " +
	 "- larger-than-150: %d\n",
	 min_angle, max_angle, num_small, num_large);
  
}


/*
 * ***************************************************************************
 * Routine:  SurfaceMesh_assignActiveSites
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com), Johan Hake (hake.dev@gmail.com)
 *
 * Purpose:  Assign markers to the vertices from a list of user-defined spheres.
 *           Note that vertices might be marked twice. Such vertex will then get
 *           the marker from the last sphere.
 * ***************************************************************************
 */
public static void SurfaceMesh_assignActiveSites(SurfMesh surfmesh, ATOM[] sphere_list, 
				    int num_spheres, 
				    int[] sphere_markers)
{
  int i, n;
  float x,y,z;
  float center_x,center_y,center_z;
  float dist, radius;

  // Reset markers to zero
  SurfaceMesh.SurfaceMesh_resetVertexMarkers(surfmesh);

  // Walk through all spheres and mark the vertices
  for (i = 0; i < num_spheres; i++) {
    center_x = sphere_list[i].x;
    center_y = sphere_list[i].y;
    center_z = sphere_list[i].z;
    radius   = sphere_list[i].radius;

    for (n = 0; n < surfmesh.nv; n++) {
      x = surfmesh.vertex[n].x;
      y = surfmesh.vertex[n].y;
      z = surfmesh.vertex[n].z;
      dist = (float)Math.sqrt((x-center_x)*(x-center_x)+
		  (y-center_y)*(y-center_y)+
		  (z-center_z)*(z-center_z));

      // If the vertex n, is within the sphere mark it
      if (dist < radius){
	surfmesh.vertex_markers[n] = sphere_markers[i];
	//System.out.printf("vertex: %d marked with %d\n", n, sphere_markers[i]);
      }
    }
  }
}

/*
 * ***************************************************************************
 * Routine:  SurfaceMesh_coarse   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Coarsen a surface mesh
 * ***************************************************************************
 */
public static char SurfaceMesh_coarse(SurfMesh surfmesh,
			float coarse_rate,
			float flatness_rate, float denseness_weight,
			float max_normal_angle)
{
  int m,n,a0,b0;
  int a,b,c;
  float x,y,z;
  NPNT3 first_ngr, second_ngr, tmp_ngr, tmp_ngr1;
  int number, neighbor_number, num;
  float nx,ny,nz;
  EIGENVECT eigen_vect = biom.new EIGENVECT();
  FLTVECT eigen_value = biom.new FLTVECT();
  float average_len, max_len;
  float ratio1 = 1.0f, ratio2 = 1.0f;
  int[] face_available_list = new int[64];
  Integer face_available_index = new Integer(0);
  int[] neighbor_tmp_list = new int[64];
  float weight,angle;

  int start_index; 
  int[] vertex_index, face_index;
 
  FLTVECT pos_vect;
  float w1,w2,w3;
//  char delete_flag;
  boolean delete_flag;
//  float max_angle;
  // use object since it will be passed by reference
  Float max_angle = new Float(0);
  
  char stop;
  int vertex_num;
  boolean delete_vertex;
  int[] vertex_markers = surfmesh.vertex_markers;

  // Check if neighborlist is created
//  if (!surfmesh.neighbor_list)
  if (surfmesh.neighbor_list == null)
    SurfaceMesh.SurfaceMesh_createNeighborlist(surfmesh);

  NPNT3[] neighbor_list = surfmesh.neighbor_list;

//  vertex_index = (int *)malloc(sizeof(int)surfmesh.nv);
  vertex_index = new int[surfmesh.nv];
//  face_index = (int *)malloc(sizeof(int)surfmesh.nf);
  face_index = new int[surfmesh.nf];

  vertex_num = surfmesh.nv;

  stop = 0;
  // If using sparseness weight, calculate the average segment length of the mesh
  if (denseness_weight > 0.0){
    average_len = 0;
    for (n = 0; n < surfmesh.nf; n++) {
      a = surfmesh.face[n].a;
      b = surfmesh.face[n].b;
      c = surfmesh.face[n].c;
     
      nx = (float)Math.sqrt((surfmesh.vertex[a].x-surfmesh.vertex[b].x)*(surfmesh.vertex[a].x-surfmesh.vertex[b].x)+
    	      (surfmesh.vertex[a].y-surfmesh.vertex[b].y)*(surfmesh.vertex[a].y-surfmesh.vertex[b].y)+
    	      (surfmesh.vertex[a].z-surfmesh.vertex[b].z)*(surfmesh.vertex[a].z-surfmesh.vertex[b].z));
      ny = (float)Math.sqrt((surfmesh.vertex[a].x-surfmesh.vertex[c].x)*(surfmesh.vertex[a].x-surfmesh.vertex[c].x)+
    	      (surfmesh.vertex[a].y-surfmesh.vertex[c].y)*(surfmesh.vertex[a].y-surfmesh.vertex[c].y)+
    	      (surfmesh.vertex[a].z-surfmesh.vertex[c].z)*(surfmesh.vertex[a].z-surfmesh.vertex[c].z));
      nz = (float)Math.sqrt((surfmesh.vertex[c].x-surfmesh.vertex[b].x)*(surfmesh.vertex[c].x-surfmesh.vertex[b].x)+
    	      (surfmesh.vertex[c].y-surfmesh.vertex[b].y)*(surfmesh.vertex[c].y-surfmesh.vertex[b].y)+
    	      (surfmesh.vertex[c].z-surfmesh.vertex[b].z)*(surfmesh.vertex[c].z-surfmesh.vertex[b].z));
      average_len += (nx+ny+nz)/3.0f;
    }
    if (surfmesh.nf == 0) {
      System.out.printf("zero degree on a vertex ....\n");
      System.exit(0);
    }
    else 
      surfmesh.avglen = average_len/(float)(surfmesh.nf);
  }


  // The main loop over all vertices
  for (n = 0; n < surfmesh.nv; n++) {
    if (surfmesh.nvm > 0 && vertex_markers[n] > 0){
      //System.out.printf("Do not remove vertex %d\n", n);
      continue;
    }

    if (((n+1) % 888) == 0 || (n+1) == surfmesh.nv) {
      System.out.printf("%2.2f%% done (%08d)          \r", 100.0*(n+1)/(float)surfmesh.nv, n+1);
 //     fflush(stdout);
      System.out.flush();
    }

    // Check if the vertex has enough neigborgs to be deleted
 //   delete_flag = 1;
    delete_flag = true;
    first_ngr = neighbor_list[n];
    while (first_ngr != null) {
      a = first_ngr.a;
      number = 0;
      num = 0;
      second_ngr = neighbor_list[a];
      while (second_ngr != null) {
	b = second_ngr.a;
	tmp_ngr = neighbor_list[n];
        while (tmp_ngr != null) {
          if (tmp_ngr.a == b)
            num++;
          tmp_ngr = tmp_ngr.next;
        }
	number++;
	second_ngr = second_ngr.next;
      }
      
      if (number <= 3 || num > 2)
//	delete_flag = 0;
    	  delete_flag = false;
      first_ngr = first_ngr.next;
    }
    
    if (delete_flag) {
      x = surfmesh.vertex[n].x;
      y = surfmesh.vertex[n].y;
      z = surfmesh.vertex[n].z;
      
      max_len = -1;
      first_ngr = neighbor_list[n];

      // If using sparseness as a criteria for coarsening
      // calculate the maximal segment length
      if (denseness_weight > 0.0){
        while (first_ngr != null) {
	  a = first_ngr.a;
	  b = first_ngr.b;
	  
	  nx = (float)Math.sqrt((x-surfmesh.vertex[a].x)*(x-surfmesh.vertex[a].x)+
		    (y-surfmesh.vertex[a].y)*(y-surfmesh.vertex[a].y)+
		    (z-surfmesh.vertex[a].z)*(z-surfmesh.vertex[a].z));
	  ny = (float)Math.sqrt((x-surfmesh.vertex[b].x)*(x-surfmesh.vertex[b].x)+
		    (y-surfmesh.vertex[b].y)*(y-surfmesh.vertex[b].y)+
		    (z-surfmesh.vertex[b].z)*(z-surfmesh.vertex[b].z));
	  if (nx > max_len)
	    max_len = nx;
	  if (ny > max_len)
	    max_len = ny;
	  
	  first_ngr = first_ngr.next;
        }
	// Max segment length over the average segment length of the mesh
	ratio2 = max_len/surfmesh.avglen;
	ratio2 = (float)Math.pow(ratio2, denseness_weight);
      }
      
      // If using curvatory as a coarsening criteria
      // calculate the local structure tensor
      if (flatness_rate > 0.0) {
//        eigen_vect = GetEigenVector(surfmesh, n, eigen_value, &max_angle);
    	  // passing max_angle as a Float object (by reference)
    	  eigen_vect = GetEigenVector(surfmesh, n, eigen_value, max_angle);
	
        if ((eigen_vect.x1==0 && eigen_vect.y1==0 && eigen_vect.z1==0) ||
	    (eigen_vect.x2==0 && eigen_vect.y2==0 && eigen_vect.z2==0) ||
	    (eigen_vect.x3==0 && eigen_vect.y3==0 && eigen_vect.z3==0))
	  ratio1 = 999999.0f;
        else {
	  if (eigen_value.x == 0) {
	    System.out.printf("max eigen_value is zero.... \n");
	    System.exit(0);
	  }
	  else {
	    ratio1 = Math.abs((eigen_value.y)/(eigen_value.x));
	    ratio1 = (float)Math.pow(ratio1, flatness_rate);
	    //ratio1 = (1.0-max_angle)*fabs((eigen_value.y)/(eigen_value.x));
	  }
        }
      }
      
      // Compare the two coarseness criterias against the given coarse_rate
      delete_vertex = ratio1*ratio2 < coarse_rate;

      // Use maximal angle between vertex normal as a complementary coarse criteria
      if (max_normal_angle > 0)
	delete_vertex = delete_vertex && max_angle > max_normal_angle;
      
      // Deleting a vertex and retrianglulate the hole
      if (delete_vertex) {
	vertex_num--;
	/* delete vertex n */
	surfmesh.vertex[n].x = -99999;
	surfmesh.vertex[n].y = -99999;
	surfmesh.vertex[n].z = -99999;
	
	neighbor_number = 0;
	first_ngr = neighbor_list[n];
	while (first_ngr != null) {
	  a = first_ngr.a;
	  c = first_ngr.c;
	  face_available_list[neighbor_number] = c;
	  neighbor_tmp_list[neighbor_number] = a;
	  neighbor_number++;
	  
	  /* delete faces associated with vertex n */
	  surfmesh.face[c].a = -1;
	  surfmesh.face[c].b = -1;
	  surfmesh.face[c].c = -1;
	  
	  /* delete neighbors associated with vertex n */
	  second_ngr = neighbor_list[a];
	  tmp_ngr = second_ngr;
	  while (second_ngr != null) {
	    if (second_ngr.a == n || second_ngr.b == n) {
	      if (second_ngr == neighbor_list[a]) {
		neighbor_list[a] = second_ngr.next;
//		free(second_ngr);
		second_ngr = null;
		second_ngr = neighbor_list[a];
		tmp_ngr = second_ngr;
	      }
	      else {
		tmp_ngr.next = second_ngr.next;
//		free(second_ngr);
		second_ngr = null;
     		second_ngr = tmp_ngr.next;
	      }
	    }
	    else {
	      if (second_ngr == neighbor_list[a]) {
		second_ngr = second_ngr.next;
	      }
	      else {
		tmp_ngr = second_ngr;
		second_ngr = second_ngr.next;
	      }
	    }
	  }
	  
	  number = 0;
	  second_ngr = neighbor_list[a];
	  while (second_ngr != null) {
	    number++;
	    second_ngr = second_ngr.next;
	  }
	  first_ngr.b = number;
	  
	  first_ngr = first_ngr.next;
	}
	
	first_ngr = neighbor_list[n];
	while (first_ngr.next != null) 
	  first_ngr = first_ngr.next;
	first_ngr.next = neighbor_list[n];
	
	face_available_index = 0;
//	PolygonSubdivision(surfmesh, neighbor_list[n], face_available_list, &face_available_index);
	// use face_availble_index as an Integer object 
	PolygonSubdivision(surfmesh, neighbor_list[n], face_available_list, face_available_index);
	
	/* order the neighbors */
	for (m = 0; m < neighbor_number; m++) {
	  first_ngr = neighbor_list[neighbor_tmp_list[m]];
	  c = first_ngr.a;
	  while (first_ngr != null) {
	    a = first_ngr.a;
	    b = first_ngr.b;

	    second_ngr = first_ngr.next;
	    while (second_ngr != null) {
	      a0 = second_ngr.a;
	      b0 = second_ngr.b;

	      // Assume counter clockwise orientation
	      if (a0==b && b0!=a) {
		tmp_ngr = first_ngr;
		while (tmp_ngr != null) {
		  if (tmp_ngr.next == second_ngr) {
		    tmp_ngr.next = second_ngr.next;
		    break;
		  }
		  tmp_ngr = tmp_ngr.next;
		}
		tmp_ngr = first_ngr.next;
		first_ngr.next = second_ngr;
		second_ngr.next = tmp_ngr;
		break;
	      }

	      second_ngr = second_ngr.next;
	    }
	    if (first_ngr.next == null) {
	      if (first_ngr.b != c) {
		System.out.printf("some polygons are not closed: %d \n",n);
		// exit(0);
	      }
	    }
	    
	    first_ngr = first_ngr.next;
	  }
	}
	
	/* Smooth the neighbors */
	for (m = 0; m < neighbor_number; m++) {
	  num = neighbor_tmp_list[m];
	  x = surfmesh.vertex[num].x;
	  y = surfmesh.vertex[num].y;
	  z = surfmesh.vertex[num].z;
	  nx = 0;
	  ny = 0;
	  nz = 0;
	  
	  weight = 0;
	  first_ngr = neighbor_list[num];
	  while (first_ngr != null) {
	    a = first_ngr.a;
	    b = first_ngr.b;
	    second_ngr = first_ngr.next;
	    if (second_ngr == null)
	      second_ngr = neighbor_list[num];
	    c = second_ngr.b;
	    pos_vect = GetPositionSurfaceOnly(x,y,z,b,a,c,surfmesh);
	    angle = GetDotProduct(surfmesh,b,a,c);
	    angle += 1.0;
	    nx += angle*pos_vect.x;
	    ny += angle*pos_vect.y;
	    nz += angle*pos_vect.z;

	    weight += angle;
	    first_ngr = first_ngr.next;
	  }
	  
	  if (weight > 0) {
	    nx /= weight;
	    ny /= weight;
	    nz /= weight;

//	    eigen_vect = GetEigenVector(surfmesh, num, &eigen_value, &max_angle);
	    // pass objects instead of primitives 
	    eigen_vect = GetEigenVector(surfmesh, num, eigen_value, max_angle);
	    if ((eigen_vect.x1==0 && eigen_vect.y1==0 && eigen_vect.z1==0) ||
		(eigen_vect.x2==0 && eigen_vect.y2==0 && eigen_vect.z2==0) ||
		(eigen_vect.x3==0 && eigen_vect.y3==0 && eigen_vect.z3==0)) {
	      surfmesh.vertex[num].x = nx;
	      surfmesh.vertex[num].y = ny;
	      surfmesh.vertex[num].z = nz;
	    }
	    else {
	      nx -= x;
	      ny -= y;
	      nz -= z;
	      w1 = (float)((nx*eigen_vect.x1+ny*eigen_vect.y1+nz*eigen_vect.z1)/(1.0+eigen_value.x));
	      w2 = (float)((nx*eigen_vect.x2+ny*eigen_vect.y2+nz*eigen_vect.z2)/(1.0+eigen_value.y));
	      w3 = (float)((nx*eigen_vect.x3+ny*eigen_vect.y3+nz*eigen_vect.z3)/(1.0+eigen_value.z));
	      surfmesh.vertex[num].x = w1*eigen_vect.x1+w2*eigen_vect.x2+w3*eigen_vect.x3 + x;
	      surfmesh.vertex[num].y = w1*eigen_vect.y1+w2*eigen_vect.y2+w3*eigen_vect.y3 + y;
	      surfmesh.vertex[num].z = w1*eigen_vect.z1+w2*eigen_vect.z2+w3*eigen_vect.z3 + z;
	    }
	  }
	}
      }
    }
    /*
      if (vertex_num < MeshSizeUpperLimit) {
      stop = 1;
      break;
      }
    */
  }
  
  /* Clean the lists of nodes and faces */
  start_index = 0;
  for (n = 0; n < surfmesh.nv; n++) {
    if (surfmesh.vertex[n].x != -99999 &&
	surfmesh.vertex[n].y != -99999 &&
	surfmesh.vertex[n].z != -99999) {
      if (start_index != n) {
	surfmesh.vertex[start_index].x = surfmesh.vertex[n].x;
	surfmesh.vertex[start_index].y = surfmesh.vertex[n].y;
	surfmesh.vertex[start_index].z = surfmesh.vertex[n].z;
	neighbor_list[start_index] = neighbor_list[n];
      }
      if (surfmesh.nvm > 0)
	vertex_markers[start_index] = vertex_markers[n];
      vertex_index[n] = start_index;
      start_index++;
    }
    else {
      vertex_index[n] = -1;
    }
  }

  surfmesh.nv = start_index;

  start_index = 0;
  for (n = 0; n < surfmesh.nf; n++) {
    a = surfmesh.face[n].a;
    b = surfmesh.face[n].b;
    c = surfmesh.face[n].c;
    if (a >= 0 && b >= 0 && c >= 0 &&
	vertex_index[a] >= 0 && vertex_index[b] >= 0 && vertex_index[c] >= 0) {
      surfmesh.face[start_index].a = vertex_index[a];
      surfmesh.face[start_index].b = vertex_index[b];
      surfmesh.face[start_index].c = vertex_index[c];
      face_index[n] = start_index;
      start_index++;
    }
    else {
      face_index[n] = -1;
    }
  }
  surfmesh.nf = start_index;
  
  for (n = 0; n < surfmesh.nv; n++) {
    first_ngr = neighbor_list[n];
    while (first_ngr != null) {
      a = first_ngr.a;
      b = first_ngr.b;
      c = first_ngr.c;
      first_ngr.a = vertex_index[a];
      first_ngr.b = vertex_index[b];
      first_ngr.c = face_index[c];
      
      first_ngr = first_ngr.next;
    }
  }
  
 // free(vertex_index);
  vertex_index = null;
//  free(face_index);
  face_index = null;
  return(stop);
}

/*
 * ***************************************************************************
 * Routine:  PolygonSubdivision
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Recursively re-triangulate the "empty" polygon
 * ***************************************************************************
 */
public static void PolygonSubdivision(SurfMesh surfmesh, 
			NPNT3 start_ngr, int[] face_available_list,
			int face_available_index)
{
  NPNT3[] neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr = biom.new NPNT3();
  NPNT3 second_ngr = biom.new NPNT3();
  NPNT3 tmp_ngr, first_copy_ngr,second_copy_ngr;
  int min_num,degree;
  int face_index,number;
  int a,b,c;

  
  number = 1;
  tmp_ngr = start_ngr;
  while (tmp_ngr.next != start_ngr) {
    number++;
    tmp_ngr = tmp_ngr.next;
  }
   
  if (number < 3) {
    System.out.printf("error: number of nodes less than 3 \n");
    System.exit(0);
  }
  if (number == 3) {
    a = start_ngr.a;
    tmp_ngr = start_ngr.next;
//   free(start_ngr);
    start_ngr = null;
    start_ngr = tmp_ngr;

    b = start_ngr.a;
    tmp_ngr = start_ngr.next;
//    free(start_ngr);
    start_ngr = null;
    start_ngr = tmp_ngr;

    c = start_ngr.a;
    tmp_ngr = start_ngr.next;
 //   free(start_ngr);
    start_ngr = null;
    start_ngr = tmp_ngr;

    face_index = face_available_list[face_available_index];
    surfmesh.face[face_index].a = a;
    surfmesh.face[face_index].b = b;
    surfmesh.face[face_index].c = c;
    face_available_index += 1;

 //   first_ngr = (NPNT3 *)malloc(sizeof(NPNT3));
    first_ngr = biom.new NPNT3();
    first_ngr.a = b;
    first_ngr.b = c;
    first_ngr.c = face_index;
    first_ngr.next = neighbor_list[a];
    neighbor_list[a] = first_ngr;

//    first_ngr = (NPNT3 *)malloc(sizeof(NPNT3));
    first_ngr = biom.new NPNT3();
    first_ngr.a = c;
    first_ngr.b = a;
    first_ngr.c = face_index;
    first_ngr.next = neighbor_list[b];
    neighbor_list[b] = first_ngr;

//    first_ngr = (NPNT3 *)malloc(sizeof(NPNT3));
    first_ngr = biom.new NPNT3();
    first_ngr.a = a;
    first_ngr.b = b;
    first_ngr.c = face_index;
    first_ngr.next = neighbor_list[c];
    neighbor_list[c] = first_ngr;

  }
  else {
    tmp_ngr = start_ngr;
    min_num = tmp_ngr.b;
    first_ngr = tmp_ngr;
    tmp_ngr = tmp_ngr.next;
    while (tmp_ngr != start_ngr) {
      degree = tmp_ngr.b;
      if (degree < min_num) {
	min_num = degree;
	first_ngr = tmp_ngr;
      }
      tmp_ngr = tmp_ngr.next;
    }
    
    min_num = 99999;
    tmp_ngr = start_ngr;
    if (tmp_ngr != first_ngr &&
	tmp_ngr != first_ngr.next &&
	tmp_ngr.next != first_ngr) {
      min_num = tmp_ngr.b;
      second_ngr = tmp_ngr;
    }
    tmp_ngr = tmp_ngr.next;
    while (tmp_ngr != start_ngr) {
      degree = tmp_ngr.b;
      if (tmp_ngr != first_ngr &&
	  tmp_ngr != first_ngr.next &&
	  tmp_ngr.next != first_ngr &&
	  degree < min_num) {
	min_num = degree;
	second_ngr = tmp_ngr;
      }
      tmp_ngr = tmp_ngr.next;
    }

    first_ngr.b += 1;
    second_ngr.b += 1;
//    first_copy_ngr = (NPNT3 *)malloc(sizeof(NPNT3));
    first_copy_ngr = biom.new NPNT3();
    first_copy_ngr.a = first_ngr.a;
    first_copy_ngr.b = first_ngr.b;
//    second_copy_ngr = (NPNT3 *)malloc(sizeof(NPNT3));
    second_copy_ngr = biom.new NPNT3();
    second_copy_ngr.a = second_ngr.a;
    second_copy_ngr.b = second_ngr.b;
    tmp_ngr = first_ngr;
    while (tmp_ngr.next != first_ngr)
      tmp_ngr = tmp_ngr.next;
    tmp_ngr.next = first_copy_ngr;
    first_copy_ngr.next = second_copy_ngr;
    second_copy_ngr.next = second_ngr.next;
    second_ngr.next = first_ngr;

    PolygonSubdivision(surfmesh, first_ngr, face_available_list,face_available_index);
    PolygonSubdivision(surfmesh, first_copy_ngr, face_available_list,face_available_index);
  }

  return;
}




/*
 * ***************************************************************************
 * Routine:  MoveVerticesSurfaceOnly
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Smooth the surface mesh by moving each of the vertices
 *           using a combination of angle-based method 
 *           and local structure tensor 
 * ***************************************************************************
 */
public static void MoveVerticesSurfaceOnly(SurfMesh surfmesh, int n)
{
  int a,b,c;
  float x,y,z;
  FLTVECT pos_vect;
  NPNT3 []neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr, second_ngr;
  float weight,angle;
  float nx,ny,nz;
  EIGENVECT eigen_vect = biom.new EIGENVECT();
  FLTVECT eigen_value = biom.new FLTVECT();
  float w1,w2,w3;
  Float max_angle = new Float(0);
  
  
  x = surfmesh.vertex[n].x;
  y = surfmesh.vertex[n].y;
  z = surfmesh.vertex[n].z;
  
  nx = 0;
  ny = 0;
  nz = 0;
  
  weight = 0;
  first_ngr = neighbor_list[n];
  while (first_ngr != null) {
    a = first_ngr.a;
    b = first_ngr.b;
    second_ngr = first_ngr.next;
    if (second_ngr == null)
      second_ngr = neighbor_list[n];
    c = second_ngr.b;
    pos_vect = GetPositionSurfaceOnly(x,y,z,b,a,c,surfmesh);
    angle = GetDotProduct(surfmesh,b,a,c);
    angle += 1.0;
    nx += angle*pos_vect.x;
    ny += angle*pos_vect.y;
    nz += angle*pos_vect.z;
    
    weight += angle;
    first_ngr = first_ngr.next;
  }
  
  if (weight > 0) {
    nx /= weight;
    ny /= weight;
    nz /= weight;

 //   eigen_vect = GetEigenVector(surfmesh, n, &eigen_value, &max_angle);
    // pass objects for eigen_value and max_angle
    eigen_vect = GetEigenVector(surfmesh, n, eigen_value, max_angle);
    if ((eigen_vect.x1==0 && eigen_vect.y1==0 && eigen_vect.z1==0) ||
	(eigen_vect.x2==0 && eigen_vect.y2==0 && eigen_vect.z2==0) ||
	(eigen_vect.x3==0 && eigen_vect.y3==0 && eigen_vect.z3==0)) {
      //System.out.printf("old point (%0.2f, %0.2f, %0.2f), new point (%0.2f, %0.2f, %0.2f)\n",
      //	     surfmesh.vertex[n].x, surfmesh.vertex[n].y, surfmesh.vertex[n].z,
      //	     nx, ny, nz);
      surfmesh.vertex[n].x = nx;
      surfmesh.vertex[n].y = ny;
      surfmesh.vertex[n].z = nz;
      
    }
    else {
      nx -= x;
      ny -= y;
      nz -= z;
      w1 = (float)((nx*eigen_vect.x1+ny*eigen_vect.y1+nz*eigen_vect.z1)/(1.0+eigen_value.x));
      w2 = (float)((nx*eigen_vect.x2+ny*eigen_vect.y2+nz*eigen_vect.z2)/(1.0+eigen_value.y));
      w3 = (float)((nx*eigen_vect.x3+ny*eigen_vect.y3+nz*eigen_vect.z3)/(1.0+eigen_value.z));
      nx = w1*eigen_vect.x1+w2*eigen_vect.x2+w3*eigen_vect.x3 + x;
      ny = w1*eigen_vect.y1+w2*eigen_vect.y2+w3*eigen_vect.y3 + y;
      nz = w1*eigen_vect.z1+w2*eigen_vect.z2+w3*eigen_vect.z3 + z;

      //System.out.printf("old point (%0.2f, %0.2f, %0.2f), new point (%0.2f, %0.2f, %0.2f)\n",
      //	     surfmesh.vertex[n].x, surfmesh.vertex[n].y, surfmesh.vertex[n].z,
      //	     nx, ny, nz);

      surfmesh.vertex[n].x = nx;
      surfmesh.vertex[n].y = ny;
      surfmesh.vertex[n].z = nz;
    }
  }
}




/*
 * ***************************************************************************
 * Routine:  GetPositionSurfaceOnly   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Move each of the vertices based on the angle-based method 
 * ***************************************************************************
 */
static FLTVECT GetPositionSurfaceOnly(float x, float y, float z, int a, int b, int c, SurfMesh surfmesh)
{
  float ax,ay,az;
  float bx,by,bz;
  float cx,cy,cz;
  float xx,yy,zz;
  float distance;
  FLTVECT tmp = biom.new FLTVECT();
  float tx,ty,tz;

  
  ax = surfmesh.vertex[a].x;
  ay = surfmesh.vertex[a].y;
  az = surfmesh.vertex[a].z;
  bx = surfmesh.vertex[b].x;
  by = surfmesh.vertex[b].y;
  bz = surfmesh.vertex[b].z;
  cx = surfmesh.vertex[c].x;
  cy = surfmesh.vertex[c].y;
  cz = surfmesh.vertex[c].z;
  
  bx -= ax;
  by -= ay;
  bz -= az;
  distance = (float)Math.sqrt(bx*bx+by*by+bz*bz);
  if (distance > 0) {
    bx /= distance;
    by /= distance;
    bz /= distance;
  }
  cx -= ax;
  cy -= ay;
  cz -= az;
  distance = (float)Math.sqrt(cx*cx+cy*cy+cz*cz);
  if (distance > 0) {
    cx /= distance;
    cy /= distance;
    cz /= distance;
  }
  tx = 0.5f*(cx+bx);
  ty = 0.5f*(cy+by);
  tz = 0.5f*(cz+bz);
  distance = (float)Math.sqrt(tx*tx+ty*ty+tz*tz);
  if (distance > 0) {
    tx /= distance;
    ty /= distance;
    tz /= distance;
  }
  xx = by*cz-bz*cy;
  yy = bz*cx-bx*cz;
  zz = bx*cy-by*cx;
  distance = (float)Math.sqrt(xx*xx+yy*yy+zz*zz);
  if (distance > 0) {
    xx /= distance;
    yy /= distance;
    zz /= distance;
  }
  bx = xx;
  by = yy;
  bz = zz;
  
  distance = tx*(x-ax)+ty*(y-ay)+tz*(z-az);
  xx = distance*tx + ax;
  yy = distance*ty + ay;
  zz = distance*tz + az;

  distance = bx*(x-xx)+by*(y-yy)+bz*(z-zz);
  tmp.x = distance*bx + xx;
  tmp.y = distance*by + yy;
  tmp.z = distance*bz + zz;

  return(tmp);
}
  

/*
 * ***************************************************************************
 * Routine:  SurfaceMesh_getMinMaxAngles
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the minimum and maximum angles in a surface mesh 
 * ***************************************************************************
 */
public static void SurfaceMesh_getMinMaxAngles(SurfMesh surfmesh, Float minangle, 
				 Float maxangle, Integer num_small, Integer num_large, 
				 int max_min_angle, int min_max_angle)
{
  int n,num1,num2;
  int a,b,c;
  float min_angle, max_angle;
  float angle;


  min_angle = 99999.0f;
  max_angle = -99999.0f;
  num1 = 0;
  num2 = 0;
  for (n = 0; n < surfmesh.nf; n++) {
    a = surfmesh.face[n].a;
    b = surfmesh.face[n].b;
    c = surfmesh.face[n].c;
     
    angle = GetAngleSurfaceOnly(surfmesh,a,b,c);
    if (angle != -999) {
      if (angle < min_angle)
	min_angle = angle;
      if (angle > max_angle)
	max_angle = angle;
      if (angle < max_min_angle)
	num1++;
      if (angle > min_max_angle)
	num2++;
    }
    angle = GetAngleSurfaceOnly(surfmesh,b,a,c);
    if (angle != -999) {
      if (angle < min_angle)
	min_angle = angle;
      if (angle > max_angle)
	max_angle = angle;
      if (angle < max_min_angle)
        num1++;
      if (angle > min_max_angle)
        num2++;
    }
    angle = GetAngleSurfaceOnly(surfmesh,c,a,b);
    if (angle != -999) {
      if (angle < min_angle)
	min_angle = angle;
      if (angle > max_angle)
	max_angle = angle;
      if (angle < max_min_angle)
        num1++;
      if (angle > min_max_angle)
        num2++;
    }
  }
  
  minangle = min_angle;
  maxangle = max_angle;
  num_small = num1;
  num_large = num2;
}



/*
 * ***************************************************************************
 * Routine:  GetAngleSurfaceOnly   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the angle defined by three vertices 
 * ***************************************************************************
 */
public static float GetAngleSurfaceOnly(SurfMesh surfmesh, int a, int b, int c)
{
  float ax,ay,az;
  float bx,by,bz;
  float cx,cy,cz;
  float length1,length2,length3;
  float angle;
  
  
  ax = surfmesh.vertex[a].x;
  ay = surfmesh.vertex[a].y;
  az = surfmesh.vertex[a].z;
  bx = surfmesh.vertex[b].x;
  by = surfmesh.vertex[b].y;
  bz = surfmesh.vertex[b].z;
  cx = surfmesh.vertex[c].x;
  cy = surfmesh.vertex[c].y;
  cz = surfmesh.vertex[c].z;

  
  length1 = (ax-bx)*(ax-bx)+(ay-by)*(ay-by)+(az-bz)*(az-bz);
  length2 = (ax-cx)*(ax-cx)+(ay-cy)*(ay-cy)+(az-cz)*(az-cz);
  length3 = (bx-cx)*(bx-cx)+(by-cy)*(by-cy)+(bz-cz)*(bz-cz);
  if (length1 == 0 || length2 == 0)
    angle = -999;
  else {
    angle = 0.5f*(length1+length2-length3)/(float)Math.sqrt(length1*length2);
    angle = (float)Math.acos(angle)*180.0f/biom.PIE;
  }

  return(angle);
}




/*
 * ***************************************************************************
 * Routine:  GetEigenVector
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the eigenvalues and eigenvectors of 
 *           the local structure tensor in a neighborhood of "index0" 
 * ***************************************************************************
 */
// public static EIGENVECT GetEigenVector(SurfMesh surfmesh,
//			 int index0, FLTVECT eigen_value, float *max_ang)
// use Float instead of float for max_ang to pass by reference
public static EIGENVECT GetEigenVector(SurfMesh surfmesh,
int index0, FLTVECT eigen_value, Float max_ang)
{
  int index,dist;
  int n,m;
  double x1, x2, x3;
  double a,b,Q;
  double c0,c1,c2;
  double[][] A = new double[3][3];
  double[] B = new double[6];
  double theta, p;
  // TODO tx and ty variables were not initialized but are used by original C code
  double tx = 0;
  double ty = 0;
  double tz = 0;
  EIGENVECT tmp = biom.new EIGENVECT();
  FLTVECT normal;
  FLTVECT normal0 = biom.new FLTVECT();
  
  int[] IndexArray = new int[333];
  int[] DistArray = new int[333];
  int start_ptr,end_ptr;
  int visited;
  NPNT3 []neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr;
  float angle, max_angle;


  normal = GetNormals(surfmesh, index0);
  A[0][0] = normal.x*normal.x;
  A[0][1] = normal.x*normal.y;
  A[0][2] = normal.x*normal.z;
  A[1][1] = normal.y*normal.y;
  A[1][2] = normal.y*normal.z;
  A[2][2] = normal.z*normal.z;

  start_ptr = 0;
  end_ptr = 1;
  IndexArray[start_ptr] = index0;
  DistArray[start_ptr] = 0;

  max_angle = 99999.0f;
  normal0.x = normal.x;
  normal0.y = normal.y;
  normal0.z = normal.z;
  while (start_ptr < end_ptr) {
    index = IndexArray[start_ptr];
    dist = DistArray[start_ptr];
    start_ptr ++;

    if (dist < ((biom.DIM_SCALE>2) ? (3):(2))) {
      first_ngr = neighbor_list[index];
      while (first_ngr != null) {
	m = first_ngr.a;
	visited = 0;
	for (n = 0; n < end_ptr; n++) {
	  if (IndexArray[n] == m) {
	    visited = 1;
	    break;
	  }
	}
	if (visited == 0) {
	  normal = GetNormals(surfmesh, m);
	  angle = normal0.x*normal.x+normal0.y*normal.y+normal0.z*normal.z;
	  if (angle < 0)
	    angle = -angle;
	  if (angle < max_angle)
	    max_angle = angle;
	  A[0][0] += normal.x*normal.x;
	  A[0][1] += normal.x*normal.y;
	  A[0][2] += normal.x*normal.z;
	  A[1][1] += normal.y*normal.y;
	  A[1][2] += normal.y*normal.z;
	  A[2][2] += normal.z*normal.z;
	  IndexArray[end_ptr] = m;
	  DistArray[end_ptr] = dist+1;
	  end_ptr ++;
	}
	first_ngr = first_ngr.next;
      }
    }
  }
  max_ang = max_angle;

  A[1][0] = A[0][1];
  A[2][0] = A[0][2];
  A[2][1] = A[1][2];
  
  c0 = A[0][0]*A[1][1]*A[2][2]+2*A[0][1]*A[0][2]*A[1][2]-A[0][0]*A[1][2]*A[1][2]
    -A[1][1]*A[0][2]*A[0][2]-A[2][2]*A[0][1]*A[0][1];
  c1 = A[0][0]*A[1][1]-A[0][1]*A[0][1]+A[0][0]*A[2][2]-
    A[0][2]*A[0][2]+A[1][1]*A[2][2]-A[1][2]*A[1][2];
  c2 = A[0][0]+A[1][1]+A[2][2];
  
  a = (3.0*c1-c2*c2)/3.0;
  b = (-2.0*c2*c2*c2+9.0*c1*c2-27.0*c0)/27.0;
  Q = b*b/4.0+a*a*a/27.0;
  	    
  theta = Math.atan2(Math.sqrt(-Q),-0.5*b);
  p = Math.sqrt(0.25*b*b-Q);
  x1 = c2/3.0+2.0*Math.pow(p,1.0/3.0)*Math.cos(theta/3.0);
  x2 = c2/3.0-Math.pow(p,1.0/3.0)*(Math.cos(theta/3.0)+Math.sqrt(3.0)*Math.sin(theta/3.0));
  x3 = c2/3.0-Math.pow(p,1.0/3.0)*(Math.cos(theta/3.0)-Math.sqrt(3.0)*Math.sin(theta/3.0));

//  if (isnan(x1) || isnan(x2) || isnan(x3)) {
  if (Double.isNaN(x1) || Double.isNaN(x2) || Double.isNaN(x3)) {
    tmp.x1 = 0;
    tmp.y1 = 0;
    tmp.z1 = 0;
    tmp.x2 = 0;
    tmp.y2 = 0;
    tmp.z2 = 0;
    tmp.x3 = 0;
    tmp.y3 = 0;
    tmp.z3 = 0;
  }
  else {
    tx = Math.max(x1,Math.max(x2,x3));
    if (tx == x1) {
      if (x2 >= x3) {
	ty = x2;
	tz = x3;
      }
      else {
	ty = x3;
	tz = x2;
      }
    }
    else if (tx == x2) {
      if (x1 >= x3) {
	ty = x1;
	tz = x3;
      }
      else {
	ty = x3;
	tz = x1;
      }
    }
    else if (tx == x3) {
      if (x1 >= x2) {
	ty = x1;
	tz = x2;
      }
      else {
	ty = x2;
	tz = x1;
      }
    }
    x1 = tx;
    x2 = ty;
    x3 = tz;
    eigen_value.x = (float)tx;
    eigen_value.y = (float)ty;
    eigen_value.z = (float)tz;
    
    if (x1 > 99999 || x1 < -99999 ||
	x2 > 99999 || x2 < -99999 ||
	x3 > 99999 || x3 < -99999) {
      System.out.printf("dsadsadsad: %f %f %f\n",x1,x2,x3);
      System.exit(0);
    }

    
    A[0][0] -= x1;
    A[1][1] -= x1;
    A[2][2] -= x1;
    B[0] = A[1][1]*A[2][2]-A[1][2]*A[1][2];
    B[1] = A[0][2]*A[1][2]-A[0][1]*A[2][2];
    B[2] = A[0][0]*A[2][2]-A[0][2]*A[0][2];
    B[3] = A[0][1]*A[1][2]-A[0][2]*A[1][1];
    B[4] = A[0][1]*A[0][2]-A[1][2]*A[0][0];
    B[5] = A[0][0]*A[1][1]-A[0][1]*A[0][1];
    c0 = B[0]*B[0]+B[1]*B[1]+B[3]*B[3];
    c1 = B[1]*B[1]+B[2]*B[2]+B[4]*B[4];
    c2 = B[3]*B[3]+B[4]*B[4]+B[5]*B[5];
    if (c0 >= c1 && c0 >= c2) {
      tx = B[0];
      ty = B[1];
      tz = B[3];
    }
    else if (c1 >= c0 && c1 >= c2) {
      tx = B[1];
      ty = B[2];
      tz = B[4];
    }
    else if (c2 >= c0 && c2 >= c1) {
      tx = B[3];
      ty = B[4];
      tz = B[5];
    }
    p = Math.sqrt(tx*tx+ty*ty+tz*tz);
    if (p > 0) {
      tx /= p;
      ty /= p;
      tz /= p;
    }
    tmp.x1 = (float) tx;
    tmp.y1 = (float) ty;
    tmp.z1 = (float) tz;
    A[0][0] += x1;
    A[1][1] += x1;
    A[2][2] += x1;
    
    
    A[0][0] -= x2;
    A[1][1] -= x2;
    A[2][2] -= x2;
    B[0] = A[1][1]*A[2][2]-A[1][2]*A[1][2];
    B[1] = A[0][2]*A[1][2]-A[0][1]*A[2][2];
    B[2] = A[0][0]*A[2][2]-A[0][2]*A[0][2];
    B[3] = A[0][1]*A[1][2]-A[0][2]*A[1][1];
    B[4] = A[0][1]*A[0][2]-A[1][2]*A[0][0];
    B[5] = A[0][0]*A[1][1]-A[0][1]*A[0][1];
    c0 = B[0]*B[0]+B[1]*B[1]+B[3]*B[3];
    c1 = B[1]*B[1]+B[2]*B[2]+B[4]*B[4];
    c2 = B[3]*B[3]+B[4]*B[4]+B[5]*B[5];
    if (c0 >= c1 && c0 >= c2) {
      tx = B[0];
      ty = B[1];
      tz = B[3];
    }
    else if (c1 >= c0 && c1 >= c2) {
      tx = B[1];
      ty = B[2];
      tz = B[4];
    }
    else if (c2 >= c0 && c2 >= c1) {
      tx = B[3];
      ty = B[4];
      tz = B[5];
    }
    p = Math.sqrt(tx*tx+ty*ty+tz*tz);
    if (p > 0) {
      tx /= p;
      ty /= p;
      tz /= p;
    }
    tmp.x2 = (float) tx;
    tmp.y2 = (float) ty;
    tmp.z2 = (float) tz;
    
    tmp.x3 = (float) (tmp.y1*tz-tmp.z1*ty);
    tmp.y3 = (float) (tmp.z1*tx-tmp.x1*tz);
    tmp.z3 = (float) (tmp.x1*ty-tmp.y1*tx);
  }

  return(tmp);
}




/*
 * ***************************************************************************
 * Routine:  GetNormals   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the normal vector at vertex "n" 
 * ***************************************************************************
 */
public static FLTVECT GetNormals(SurfMesh surfmesh, int n)
{
  int a,b;
  float x,y,z;
  NPNT3[] neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr;
  int number;
  FLTVECT normal = biom.new FLTVECT();
  float gx,gy,gz;
  float ax,ay,az;
  float bx,by,bz;
  float length;
 
  
  
  x = surfmesh.vertex[n].x;
  y = surfmesh.vertex[n].y;
  z = surfmesh.vertex[n].z;
  
  number = 0;
  normal.x = 0;
  normal.y = 0;
  normal.z = 0;
  first_ngr = neighbor_list[n];
  while (first_ngr != null) {
    a = first_ngr.a;
    b = first_ngr.b;
    
    ax = surfmesh.vertex[a].x-x;
    ay = surfmesh.vertex[a].y-y;
    az = surfmesh.vertex[a].z-z;
    length = (float)Math.sqrt(ax*ax+ay*ay+az*az);
    if (length > 0) {
      ax /= length;
      ay /= length;
      az /= length;
    }
    bx = surfmesh.vertex[b].x-x;
    by = surfmesh.vertex[b].y-y;
    bz = surfmesh.vertex[b].z-z;
    length = (float)Math.sqrt(bx*bx+by*by+bz*bz);
    if (length > 0) {
      bx /= length;
      by /= length;
      bz /= length;
    }
    gx = ay*bz-az*by;
    gy = az*bx-ax*bz;
    gz = ax*by-ay*bx;
    length = (float)Math.sqrt(gx*gx+gy*gy+gz*gz);
    if (length > 0) {
      gx /= length;
      gy /= length;
      gz /= length;
    }
    length = normal.x*gx+normal.y*gy+normal.z*gz;
    if (length < 0) {
      gx = -gx;
      gy = -gy;
      gz = -gz;
    }
    normal.x += gx;
    normal.y += gy;
    normal.z += gz;
    
    number ++;
    first_ngr = first_ngr.next;
  }
  
  if (number > 0) {
    normal.x /= (float)number;
    normal.y /= (float)number;
    normal.z /= (float)number;
    length = (float)Math.sqrt(normal.x*normal.x+normal.y*normal.y+normal.z*normal.z);
    if (length > 0) {
      normal.x /= length;
      normal.y /= length;
      normal.z /= length;
    }
  }
  else {
    normal.x = 0;
    normal.y = 0;
    normal.z = 0;
  }

  return(normal);
}




/*
 * ***************************************************************************
 * Routine:  CheckFlipAction
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Check if the edge flipping is needed or not, by the 
 *           "smaller angle criterion" 
 * ***************************************************************************
 */
// public static char CheckFlipAction(SurfMesh surfmesh,
public static boolean CheckFlipAction(SurfMesh surfmesh,
		     int a, int b, int c, int d)
{
  NPNT3[] neighbor_list = surfmesh.neighbor_list;
  float min_angle1,min_angle2,angle;
  
  /* smaller angle criterion */
  min_angle1 = -99999;
  angle = GetDotProduct(surfmesh, a,b,c);
  if (angle > min_angle1)
    min_angle1 = angle;
  angle = GetDotProduct(surfmesh, a,b,d);
  if (angle > min_angle1)
    min_angle1 = angle;
  angle = GetDotProduct(surfmesh, b,a,c);
  if (angle > min_angle1)
    min_angle1 = angle;
  angle = GetDotProduct(surfmesh, b,a,d);
  if (angle > min_angle1)
    min_angle1 = angle;
  
  min_angle2 = -99999;
  angle = GetDotProduct(surfmesh, c,a,d);
  if (angle > min_angle2)
    min_angle2 = angle;
  angle = GetDotProduct(surfmesh, c,b,d);
  if (angle > min_angle2)
    min_angle2 = angle;
  angle = GetDotProduct(surfmesh, d,a,c);
  if (angle > min_angle2)
    min_angle2 = angle;
  angle = GetDotProduct(surfmesh, d,b,c);
  if (angle > min_angle2)
    min_angle2 = angle;
  
  if (min_angle1 > min_angle2)
//    return(1);
	  return true;
  else
 //   return(0);
	  return false;
  
}




/*
 * ***************************************************************************
 * Routine:  GetDotProduct   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the dot product of two vectors (b-a) and (c-a) 
 * ***************************************************************************
 */
public static float GetDotProduct(SurfMesh surfmesh,int a, int b, int c)
{
  float cx,cy,cz;
  float bx,by,bz;
  float length;


  bx = surfmesh.vertex[b].x-surfmesh.vertex[a].x;
  by = surfmesh.vertex[b].y-surfmesh.vertex[a].y;
  bz = surfmesh.vertex[b].z-surfmesh.vertex[a].z;
  length = (float)Math.sqrt(bx*bx+by*by+bz*bz);
  if (length > 0) {
    bx /= length;
    by /= length;
    bz /= length;
  }
  
  cx = surfmesh.vertex[c].x-surfmesh.vertex[a].x;
  cy = surfmesh.vertex[c].y-surfmesh.vertex[a].y;
  cz = surfmesh.vertex[c].z-surfmesh.vertex[a].z;
  length = (float)Math.sqrt(cx*cx+cy*cy+cz*cz);
  if (length > 0) {
    cx /= length;
    cy /= length;
    cz /= length;
  }
  
  length = bx*cx+by*cy+bz*cz;
  return(length);
}





/*
 * ***************************************************************************
 * Routine:  EdgeFlipping
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Perform the edge flipping 
 * ***************************************************************************
 */
public static void EdgeFlipping(SurfMesh surfmesh, int n)
{
  int a,b,c;
  NPNT3[] neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr,second_ngr;
  NPNT3 tmp_ngr1;
  NPNT3 tmp_ngr2 = biom.new NPNT3();
  NPNT3 tmp_ngr;
 // char flip_flag,flip_check;
  boolean flip_flag, flip_check;
  int f1,f2, number;
  float ax,ay,az;
  
  
  first_ngr = neighbor_list[n];
  while (first_ngr != null) {

    number = 0;
    tmp_ngr = neighbor_list[n];
    while (tmp_ngr != null) {
      number++;
      tmp_ngr = tmp_ngr.next;
    }
    if (number <= 3) {
      if (number > 0) {
	ax = 0;
	ay = 0;
	az = 0;
	tmp_ngr = neighbor_list[n];
	while (tmp_ngr != null) {
	  a = tmp_ngr.a;
	  ax += surfmesh.vertex[a].x;
	  ay += surfmesh.vertex[a].y;
	  az += surfmesh.vertex[a].z;
	  tmp_ngr = tmp_ngr.next;
	}
	
	surfmesh.vertex[n].x = ax/(float)number;
	surfmesh.vertex[n].y = ay/(float)number;
	surfmesh.vertex[n].z = az/(float)number;
      }
      return;
    }

    a = first_ngr.a;
    b = first_ngr.b;
    second_ngr = first_ngr.next;
    if (second_ngr == null)
      second_ngr = neighbor_list[n];
    c = second_ngr.b;

 //   flip_flag = 1;
    flip_flag = true;
    number = 0;
    tmp_ngr = neighbor_list[b];
    while (tmp_ngr != null) {
      number++;
      tmp_ngr = tmp_ngr.next;
    }
    if (number <= 3)
 //     flip_flag = 0;
    	flip_flag = false;

    tmp_ngr = neighbor_list[a];
    while (tmp_ngr != null) {
      if (tmp_ngr.a == c)
//	flip_flag = 0;
    	  flip_flag = false;
      tmp_ngr = tmp_ngr.next;
    }
    tmp_ngr = neighbor_list[c];
    while (tmp_ngr != null) {
      if (tmp_ngr.a == a)
//	flip_flag = 0;
    	  flip_flag = false;
      tmp_ngr = tmp_ngr.next;
    }

    if (flip_flag) {
    
      flip_check = CheckFlipAction(surfmesh,n,b,a,c);
      
      if (flip_check) {
	f1 = first_ngr.c;
	f2 = second_ngr.c;
	
	/* Update face info */
	surfmesh.face[f1].a = n;
	surfmesh.face[f1].b = a; 
	surfmesh.face[f1].c = c; 
	surfmesh.face[f2].a = b; 
	surfmesh.face[f2].b = c; // Switch a and c here to make the face 
	surfmesh.face[f2].c = a; // normal point outward		  
	
	/* Delete the entries in neighbor lists */
	first_ngr.b = c;
	if (first_ngr.next == null) 
	  neighbor_list[n] = neighbor_list[n].next;
	else
	  first_ngr.next = second_ngr.next;
	tmp_ngr1 = second_ngr;

	tmp_ngr = neighbor_list[b];
	while (tmp_ngr != null) {
	  if (tmp_ngr.b == n) 
	    break;
	  tmp_ngr = tmp_ngr.next;
	}
	if (tmp_ngr == null)
	  System.out.printf("my god ... %d\n",n);
	if (tmp_ngr.a == c) {
	  tmp_ngr.b = a;
	  tmp_ngr.c = f2;
	  if (tmp_ngr.next == null) {
	    second_ngr = neighbor_list[b];
	    neighbor_list[b] = second_ngr.next;
	  }
	  else {
	    second_ngr = tmp_ngr.next;
	    tmp_ngr.next = second_ngr.next;
	  }
	  tmp_ngr2 = second_ngr;
	}
	else 
	{
	  System.out.printf("delete error!!! %d : %d %d %d\n",n,a,b,c);
	  System.out.printf("(%f,%f,%f)\n",
		 surfmesh.vertex[n].x,
		 surfmesh.vertex[n].y,
		 surfmesh.vertex[n].z);
	}
      
	/* Add the entries in neighbor lists */
	tmp_ngr = neighbor_list[a];
	while (tmp_ngr != null) {
	  if ((tmp_ngr.a == n && tmp_ngr.b == b) ||
	      (tmp_ngr.a == b && tmp_ngr.b == n))
	    break;
	  tmp_ngr = tmp_ngr.next;
	}

	// Assume neigbors are stored counter clockwise
	if (tmp_ngr.a == b && tmp_ngr.b == n) {
	  tmp_ngr.b = c;
	  tmp_ngr.c = f2;
	  tmp_ngr1.a = c;
	  tmp_ngr1.b = n;
	  tmp_ngr1.c = f1;
	  tmp_ngr1.next = tmp_ngr.next;
	  tmp_ngr.next = tmp_ngr1;
	}
	else 
	  System.out.printf("add error 111\n");
	
	tmp_ngr = neighbor_list[c];
	while (tmp_ngr != null) {
	  if ((tmp_ngr.a == n && tmp_ngr.b == b) ||
	      (tmp_ngr.a == b && tmp_ngr.b == n))
	    break;
	  tmp_ngr = tmp_ngr.next;
	}

	// Assume neigbors are stored counter clockwise
	if (tmp_ngr.a == n && tmp_ngr.b == b) {
	  tmp_ngr.b = a;
	  tmp_ngr.c = f1;
	  tmp_ngr2.a = a;
	  tmp_ngr2.b = b;
	  tmp_ngr2.c = f2;
	  tmp_ngr2.next = tmp_ngr.next;
	  tmp_ngr.next = tmp_ngr2;
	}
	else 
	  System.out.printf("add error 222\n");
      }
    }
    
    first_ngr = first_ngr.next;
  }
  
}




/*
 * ***************************************************************************
 * Routine:  NormalSmooth
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Smooth the surface mesh by anisotropic normal-based averaging 
 * ***************************************************************************
 */
public static void NormalSmooth(SurfMesh surfmesh, int n)
{
  int a,b,c,d;
  
  // e was not initialized by used in original C code
  int e = 0;
  NPNT3[] neighbor_list = surfmesh.neighbor_list;
  NPNT3 first_ngr,second_ngr,third_ngr;
  NPNT3 tmp_ngr;
  float bx,by,bz;
  float cx,cy,cz;
  float dx,dy,dz;
  float fx,fy,fz;
  float gx,gy,gz;
  float pos_x,pos_y,pos_z;
  int number,num;
  float theta,phi,alpha;
  float length;
  FLTVECT normal,sv;
  
  
  number = 0;
  pos_x = 0;
  pos_y = 0;
  pos_z = 0;
  first_ngr = neighbor_list[n];
  while (first_ngr != null) {
    a = first_ngr.a;
    b = first_ngr.b;
    second_ngr = first_ngr.next;
    if (second_ngr == null)
      second_ngr = neighbor_list[n];
    c = second_ngr.b;
    third_ngr = second_ngr.next;
    if (third_ngr == null)
      third_ngr = neighbor_list[n];
    d = third_ngr.b;
    
    tmp_ngr = neighbor_list[b];
    while (tmp_ngr != null) {
      if ((tmp_ngr.a == c && tmp_ngr.b != n) ||
	  (tmp_ngr.b == c && tmp_ngr.a != n))
	break;
      tmp_ngr = tmp_ngr.next;
    }
    if (tmp_ngr.a == c && tmp_ngr.b != n)
      e = tmp_ngr.b;
    else if (tmp_ngr.b == c && tmp_ngr.a != n)
      e = tmp_ngr.a;
    else 
      System.out.printf("normal smoothing error...\n");

    normal = GetCrossProduct(surfmesh, n, b, c);
    gx = normal.x;
    gy = normal.y;
    gz = normal.z;
    dx = 0;
    dy = 0;
    dz = 0;

    num  = 0;
    normal = GetCrossProduct(surfmesh, n, a, b);
    length = normal.x*gx+normal.y*gy+normal.z*gz;
    if (length > 0) {
      num++;
      dx += length*normal.x;
      dy += length*normal.y;
      dz += length*normal.z;
    }
    normal = GetCrossProduct(surfmesh, n, c, d);
    length = normal.x*gx+normal.y*gy+normal.z*gz;
    if (length > 0) {
      num++;
      dx += length*normal.x;
      dy += length*normal.y;
      dz += length*normal.z;
    }
    normal = GetCrossProduct(surfmesh, b, e, c);
    length = normal.x*gx+normal.y*gy+normal.z*gz;
    if (length > 0) {
      num++;
      dx += length*normal.x;
      dy += length*normal.y;
      dz += length*normal.z;
    }

    length = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
    if (length > 0) {
      dx /= length;
      dy /= length;
      dz /= length;
      fx = gy*dz-gz*dy;
      fy = gz*dx-gx*dz;
      fz = gx*dy-gy*dx;
      cx = surfmesh.vertex[c].x;
      cy = surfmesh.vertex[c].y;
      cz = surfmesh.vertex[c].z;
      bx = surfmesh.vertex[b].x;
      by = surfmesh.vertex[b].y;
      bz = surfmesh.vertex[b].z;
      length = fx*(bx-cx)+fy*(by-cy)+fz*(bz-cz);
      if (length >= 0) {
	theta = (float)Math.atan2(by-cy,bx-cx);
	phi = (float)Math.atan2(bz-cz, Math.sqrt((bx-cx)*(bx-cx)+(by-cy)*(by-cy)));
      }
      else {
	theta = (float)Math.atan2(cy-by,cx-bx);
	phi = (float)Math.atan2(cz-bz, Math.sqrt((bx-cx)*(bx-cx)+(by-cy)*(by-cy)));
      }
      
      alpha = (float)Math.acos(dx*gx+dy*gy+dz*gz)/(float)(4.0-num);
      sv = Rotate(surfmesh.vertex[n].x-cx,surfmesh.vertex[n].y-cy,surfmesh.vertex[n].z-cz,theta,phi,alpha);
      pos_x += sv.x+cx;
      pos_y += sv.y+cy;
      pos_z += sv.z+cz;
      
      number++;
    }
    
    first_ngr = first_ngr.next;
  }
  
//  if (number > 0 && !isnan(pos_x) && !isnan(pos_y) && !isnan(pos_z)) {
  if (number > 0 && !Float.isNaN(pos_x) && !Float.isNaN(pos_y) && !Float.isNaN(pos_z)) {
    surfmesh.vertex[n].x = pos_x/(float)number;
    surfmesh.vertex[n].y = pos_y/(float)number;
    surfmesh.vertex[n].z = pos_z/(float)number;
  }
  
}



/*
 * ***************************************************************************
 * Routine:  GetCrossProduct   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Calculate the cross product vector between (b-a) and (c-a) 
 * ***************************************************************************
 */
public static FLTVECT GetCrossProduct(SurfMesh surfmesh,int a, int b, int c) 
{
  float gx,gy,gz;
  float cx,cy,cz;
  float bx,by,bz;
  float length;
  FLTVECT value = biom.new FLTVECT();


  bx = surfmesh.vertex[b].x-surfmesh.vertex[a].x;
  by = surfmesh.vertex[b].y-surfmesh.vertex[a].y;
  bz = surfmesh.vertex[b].z-surfmesh.vertex[a].z;
  length = (float)Math.sqrt(bx*bx+by*by+bz*bz);
  if (length > 0) {
    bx /= length;
    by /= length;
    bz /= length;
  }
  cx = surfmesh.vertex[c].x-surfmesh.vertex[a].x;
  cy = surfmesh.vertex[c].y-surfmesh.vertex[a].y;
  cz = surfmesh.vertex[c].z-surfmesh.vertex[a].z;
  length = (float)Math.sqrt(cx*cx+cy*cy+cz*cz);
  if (length > 0) {
    cx /= length;
    cy /= length;
    cz /= length;
  }
  gx = cy*bz-cz*by;
  gy = cz*bx-cx*bz;
  gz = cx*by-cy*bx;
  length = (float)Math.sqrt(gx*gx+gy*gy+gz*gz);
  if (length > 0) {
    gx /= length;
    gy /= length;
    gz /= length;
  }

  value.x = gx;
  value.y = gy;
  value.z = gz;

  return(value);
}


/*
 * ***************************************************************************
 * Routine:  Rotate   
 *
 * Author:   Zeyun Yu (zeyun.yu@gmail.com)
 *
 * Purpose:  Rotate a point "sx,sy,sz" around "theta,phi" axis by "angle" 
 * ***************************************************************************
 */
public static FLTVECT Rotate(float sx, float sy, float sz,
	       float theta, float phi, float angle)
{
  float x,y,z;
  float xx,yy,zz;
  float[][] a = new float[3][3];
  float[][] b = new float[3][3];
  FLTVECT tmp = biom.new FLTVECT();

  a[0][0] = (float)(Math.cos(0.5f*biom.PIE-phi)*Math.cos(theta));
  a[0][1] = (float)(Math.cos(0.5*biom.PIE-phi)*Math.sin(theta));
  a[0][2] = (float)-Math.sin(0.5*biom.PIE-phi);
  a[1][0] = (float)-Math.sin(theta);
  a[1][1] = (float)Math.cos(theta);
  a[1][2] = 0.f;
  a[2][0] = (float)(Math.sin(0.5*biom.PIE-phi)*Math.cos(theta));
  a[2][1] = (float)(Math.sin(0.5*biom.PIE-phi)*Math.sin(theta));
  a[2][2] = (float)Math.cos(0.5*biom.PIE-phi);

  b[0][0] = (float)(Math.cos(0.5*biom.PIE-phi)*Math.cos(theta));
  b[0][1] = (float)-Math.sin(theta); 
  b[0][2] = (float)(Math.sin(0.5*biom.PIE-phi)*Math.cos(theta)); 
  b[1][0] = (float)(Math.cos(0.5*biom.PIE-phi)*Math.sin(theta));
  b[1][1] = (float)Math.cos(theta);
  b[1][2] = (float)(Math.sin(0.5*biom.PIE-phi)*Math.sin(theta));
  b[2][0] = (float)-Math.sin(0.5*biom.PIE-phi);
  b[2][1] = 0.f;
  b[2][2] = (float)Math.cos(0.5*biom.PIE-phi);


  x = a[0][0]*sx+a[0][1]*sy+a[0][2]*sz;
  y = a[1][0]*sx+a[1][1]*sy+a[1][2]*sz;
  z = a[2][0]*sx+a[2][1]*sy+a[2][2]*sz;
      
  xx = (float)(Math.cos(angle)*x - Math.sin(angle)*y);
  yy = (float)(Math.sin(angle)*x + Math.cos(angle)*y);
  zz = z;

  tmp.x = b[0][0]*xx+b[0][1]*yy+b[0][2]*zz;
  tmp.y = b[1][0]*xx+b[1][1]*yy+b[1][2]*zz;
  tmp.z = b[2][0]*xx+b[2][1]*yy+b[2][2]*zz;
  
  return(tmp);
  
}
}
