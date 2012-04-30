# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{reportgrid}
  s.version = "1.0.3"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Kris Nuttycombe"]
  s.date = %q{2011-09-23}
  s.description = %q{ReportGrid Ruby Client Library
}
  s.email = ["kris [at] reportgrid [dot] com"]
  s.files = ["Rakefile", "README.rdoc", "lib/reportgrid.rb", "test/test_reportgrid.rb"]
  s.homepage = %q{http://api.reportgrid.com/ruby}
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{reportgrid}
  s.rubygems_version = %q{1.4.1}
  s.summary = %q{Ruby client library for ReportGrid (http://www.reportgrid.com)}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<json>, [">= 0"])
    else
      s.add_dependency(%q<json>, [">= 0"])
    end
  else
    s.add_dependency(%q<json>, [">= 0"])
  end
end
