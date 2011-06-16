$:.push File.expand_path(File.join(File.dirname(__FILE__), 'lib'))
require 'reportgrid'

Gem::Specification.new do |s|
  s.name              = ReportGrid::NAME
  s.version           = ReportGrid::VERSION
  s.authors           = [ReportGrid::AUTHOR]
  s.email             = [ReportGrid::AUTHOR_EMAIL]
  s.homepage          = ReportGrid::URL
  s.rubyforge_project = ReportGrid::NAME
  s.summary           = ReportGrid::DESCRIPTION
  s.description       = File.read(File.expand_path(File.join(File.dirname(__FILE__), 'README.rdoc')))

  s.add_dependency('json')

  s.files = ['Rakefile', 'README.rdoc'] + Dir['lib/*.rb'] + Dir['test/*']
end
