# -*- mode: ruby -*-
# vi: set ft=ruby :
Vagrant.configure("2") do |config|
  config.vm.box = "alvistack/ubuntu-22.04"

  # via 127.0.0.1 to disable public access
  # - docker container
  config.vm.network "forwarded_port", guest: 2375, host: 2375, host_ip: "127.0.0.1"
  # - MariaDB
  config.vm.network "forwarded_port", guest: 3306, host: 3307, host_ip: "127.0.0.1"
  # - Spring API
  config.vm.network "forwarded_port", guest: 8080, host: 8080, host_ip: "127.0.0.1", auto_correct: true

  # VM setting
  config.vm.provider "virtualbox" do |vb|
	vb.name = "spring-api"
    vb.cpus = 2
    vb.memory = "8192"
  end

  # provision
  config.vm.provision "shell", inline: <<-SHELL
    apt-get update
	apt-get install docker.io -y
  SHELL
end
