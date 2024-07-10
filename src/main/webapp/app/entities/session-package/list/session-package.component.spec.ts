import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { SessionPackageService } from '../service/session-package.service';

import { SessionPackageComponent } from './session-package.component';

describe('SessionPackage Management Component', () => {
  let comp: SessionPackageComponent;
  let fixture: ComponentFixture<SessionPackageComponent>;
  let service: SessionPackageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'session-package', component: SessionPackageComponent }]),
        HttpClientTestingModule,
        SessionPackageComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(SessionPackageComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SessionPackageComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(SessionPackageService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 'ABC' }],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.sessionPackages?.[0]).toEqual(expect.objectContaining({ id: 'ABC' }));
  });

  describe('trackId', () => {
    it('Should forward to sessionPackageService', () => {
      const entity = { id: 'ABC' };
      jest.spyOn(service, 'getSessionPackageIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getSessionPackageIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
