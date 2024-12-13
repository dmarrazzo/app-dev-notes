# Ansible

play has three keys: `name`, `hosts`, and `tasks`.


```yaml
- name: Configure important user consistently
  hosts: servera.lab.example.com
  tasks:
    - name: newbie exists with UID 4000
      ansible.builtin.user:
        name: newbie
        uid: 4000
        state: present
```

`ansible.builtin.user` is the module to run for this task. 

## Finding Modules for Tasks
Modules are the tools that plays use to accomplish tasks. Hundreds of modules have been written that do different things. You can usually find a tested, special-purpose module that does what you need, often as part of the default automation execution environment.

```
ansible-navigator collections
```

## Running Playbooks
The ansible-navigator run command is used to run playbooks. The command is executed on the control node, and the name of the playbook to be run is passed as an argument.


- `-v`	Displays task results.
- `-vv`	Displays task results and task configuration.
- `-vvv`	Displays extra information about connections to managed hosts.
- `-vvvv`	Adds extra verbosity options to the connection plug-ins, including users being used on the managed hosts to execute scripts, and what scripts have been executed.

## Playbook

- A **task** is the application of a module to perform a specific unit of work
- A **play** is a sequence of tasks to be applied

Example of a **Playbook** with 1 play and 1 task:

```yaml
---
- name: Create userA
  hosts: server_X.example.com
  tasks:
    - name: userA exists with UID 2000
      ansible.builtin.user:
        name: userA
        uid: 2000
        state: present
```

A playbook can contain different plays which runs on different `hosts`. (e.g. database and web)


## Managing Variables and Facts

`ansible-navigator` run command line by using the `--extra-vars` or `-e`

- Group variables defined in the inventory
- Group variables defined in files in a `group_vars` subdirectory in the same directory as the inventory or the playbook
- Host variables defined in files in a host_vars
- Host facts, discovered at runtime
- Play variables in the playbook (vars and vars_files)
- Task variables
- Extra variables defined on the command line

One common method is to place a variable in a vars block at the beginning of a play:

```yaml
- hosts: all
  vars:
    user: joe
    home: /home/joe
```

Using Variables in Playbooks:

```yaml
vars:
  user: joe

tasks:
  # This line will read: Creates the user joe
  - name: Creates the user {{ user }}
    user:
      # This line will create the user named Joe
      name: "{{ user }}"
```

### Host Variables and Group Variables

```ini
[servers]
demo.example.com  ansible_user=joe
```

Group variables

```ini
[servers]
demo1.example.com
demo2.example.com

[servers:vars]
user=joe
```

Defining the user group variable for the servers group, which consists of two host groups each with two servers.

```ini
[servers1]
demo1.example.com
demo2.example.com

[servers2]
demo3.example.com
demo4.example.com

[servers:children]
servers1
servers2

[servers:vars]
user=joe
```

### Using Directories to Populate Host and Group Variables

To define group variables for the `servers` group, you would create a YAML file named `group_vars/servers`

Directory structure:

```
project
├── ansible.cfg
├── group_vars
│   ├── datacenters
│   ├── datacenters1
│   └── datacenters2
├── host_vars
│   ├── demo1.example.com
│   ├── demo2.example.com
│   ├── demo3.example.com
│   └── demo4.example.com
├── inventory
└── playbook.yml
```

### Using Dictionaries as Variables

```yaml
users:
  bjones:
    first_name: Bob
    last_name: Jones
    home_dir: /users/bjones
  acook:
    first_name: Anne
    last_name: Cook
    home_dir: /users/acook
```

```
# Returns 'Bob'
users.bjones.first_name
```

```
# Returns 'Bob'
users['bjones']['first_name']
```

### Capturing Command Output with Registered Variables

```yaml
---
- name: Installs a package and prints the result
  hosts: all
  tasks:
    - name: Install the package
      ansible.builtin.dnf:
        name: httpd
        state: installed
      register: install_result

    - debug:
        var: install_result
```

### Ansible Vault

```
ansible-vault create secret.yml
New Vault password: redhat
Confirm New Vault password: redhat
```

Example project directory:

```
.
├── ansible.cfg
├── group_vars
│   └── webservers
│       └── vars
├── host_vars
│   └── demo.example.com
│       ├── vars
│       └── vault
├── inventory
└── playbook.yml
```

## Managing Facts

Facts gathered for a managed host might include:

- The host name
- The kernel version
- Network interface names
- Network interface IP addresses

```yaml
---
- name: Fact dump
  hosts: all
  tasks:
    - name: Print all facts
      ansible.builtin.debug:
        msg: >
          The default IPv4 address of {{ ansible_facts.fqdn }}
          is {{ ansible_facts.default_ipv4.address }}
```

