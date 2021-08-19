package com.yn.zhc.spring.dao;

import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class IndexDao {

	public IndexDao(){
		System.out.println("IndexDao 构建");
	}

	@PostConstruct /* PostConstruct是将此方法标识为初始化方法 */
	public void init(){
		System.out.println("IndexDao init");
	}

	public void sayHello(){
		System.out.println("Hello world,this is IndexDao");
	}

}
